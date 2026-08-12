resource "aws_ecs_cluster" "main" {
  name = "${local.name}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_service_discovery_private_dns_namespace" "main" {
  name = "${local.name}.internal"
  vpc  = aws_vpc.main.id
}

resource "aws_cloudwatch_log_group" "service" {
  for_each = local.services

  name              = "/ecs/${local.name}/${each.key}"
  retention_in_days = 30
  kms_key_id        = aws_kms_key.main.arn
}

resource "aws_service_discovery_service" "service" {
  for_each = local.services

  name = each.key

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

}

resource "aws_ecs_task_definition" "service" {
  for_each = local.services

  family                   = "${local.name}-${each.key}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = var.container_images[each.key]
      essential = true

      portMappings = [{
        containerPort = each.value.port
        hostPort      = each.value.port
        protocol      = "tcp"
      }]

      environment = [
        { name = "SERVICE_NAME", value = each.key },
        { name = "DB_HOST", value = aws_db_instance.main.address },
        { name = "DB_PORT", value = tostring(aws_db_instance.main.port) },
        { name = "DB_NAME", value = var.db_name },
        { name = "MONGO_HOST", value = aws_docdb_cluster.main.endpoint },
        { name = "MONGO_PORT", value = tostring(var.documentdb_port) },
        { name = "MONGO_DATABASE", value = var.documentdb_name },
        { name = "RABBITMQ_HOST", value = local.rabbitmq_host },
        { name = "RABBITMQ_PORT", value = tostring(var.rabbitmq_port) },
        { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/${var.db_name}" },
        { name = "SPRING_RABBITMQ_HOST", value = local.rabbitmq_host },
        { name = "SPRING_RABBITMQ_PORT", value = tostring(var.rabbitmq_port) },
        { name = "SPRING_RABBITMQ_SSL_ENABLED", value = "true" },
        { name = "Mongo__Database", value = var.documentdb_name },
        { name = "RabbitMq__Host", value = local.rabbitmq_host },
        { name = "RabbitMq__Port", value = tostring(var.rabbitmq_port) },
        { name = "RabbitMq__UseSsl", value = "true" },
        { name = "RABBITMQ_USE_SSL", value = "true" }
      ]

      secrets = [
        { name = "SPRING_DATASOURCE_USERNAME", valueFrom = "${aws_secretsmanager_secret.db.arn}:username::" },
        { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "${aws_secretsmanager_secret.db.arn}:password::" },
        { name = "SPRING_RABBITMQ_USERNAME", valueFrom = "${aws_secretsmanager_secret.rabbitmq.arn}:username::" },
        { name = "SPRING_RABBITMQ_PASSWORD", valueFrom = "${aws_secretsmanager_secret.rabbitmq.arn}:password::" },
        { name = "RabbitMq__Username", valueFrom = "${aws_secretsmanager_secret.rabbitmq.arn}:username::" },
        { name = "RabbitMq__Password", valueFrom = "${aws_secretsmanager_secret.rabbitmq.arn}:password::" },
        { name = "ConnectionStrings__Postgres", valueFrom = "${aws_secretsmanager_secret.db.arn}:postgres_connection_string::" },
        { name = "ConnectionStrings__Mongo", valueFrom = "${aws_secretsmanager_secret.documentdb.arn}:connection_string::" },
        { name = "PAYMENTS_SECURITY_API_KEY", valueFrom = "${aws_secretsmanager_secret.app.arn}:api_key::" },
        { name = "Security__ApiKey", valueFrom = "${aws_secretsmanager_secret.app.arn}:api_key::" }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.service[each.key].name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = each.key
        }
      }

    }
  ])
}

resource "aws_ecs_service" "service" {
  for_each = local.services

  name            = "${local.name}-${each.key}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.service[each.key].arn
  desired_count   = lookup(var.desired_counts, each.key, 1)
  launch_type     = "FARGATE"

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200
  enable_execute_command             = true

  network_configuration {
    subnets          = values(aws_subnet.private)[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.service[each.key].arn
    container_name   = each.key
    container_port   = each.value.port
  }

  service_registries {
    registry_arn = aws_service_discovery_service.service[each.key].arn
  }

  depends_on = [aws_lb_listener.http]
}

resource "aws_appautoscaling_target" "service" {
  for_each = local.services

  max_capacity       = each.value.max_capacity
  min_capacity       = lookup(var.desired_counts, each.key, 1)
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.service[each.key].name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "cpu" {
  for_each = local.services

  name               = "${local.name}-${each.key}-cpu"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.service[each.key].resource_id
  scalable_dimension = aws_appautoscaling_target.service[each.key].scalable_dimension
  service_namespace  = aws_appautoscaling_target.service[each.key].service_namespace

  target_tracking_scaling_policy_configuration {
    target_value       = 60
    scale_in_cooldown  = 120
    scale_out_cooldown = 60

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
  }
}
