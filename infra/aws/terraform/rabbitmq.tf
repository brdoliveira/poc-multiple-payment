resource "aws_mq_broker" "main" {
  broker_name                = "${local.name}-rabbitmq"
  engine_type                = "RabbitMQ"
  engine_version             = var.rabbitmq_engine_version
  host_instance_type         = var.rabbitmq_instance_type
  deployment_mode            = "SINGLE_INSTANCE"
  publicly_accessible        = false
  auto_minor_version_upgrade = true
  apply_immediately          = true

  subnet_ids      = [values(aws_subnet.private)[0].id]
  security_groups = [aws_security_group.rabbitmq.id]

  user {
    username = var.rabbitmq_username
    password = random_password.rabbitmq.result
  }

  logs {
    general = true
  }
}
