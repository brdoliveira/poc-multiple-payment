resource "aws_kms_key" "main" {
  description             = "KMS da arquitetura de pagamentos"
  deletion_window_in_days = 14
  enable_key_rotation     = true
}

resource "aws_kms_alias" "main" {
  name          = "alias/${local.name}"
  target_key_id = aws_kms_key.main.key_id
}

resource "aws_security_group" "alb" {
  name        = "${local.name}-alb"
  description = "Entrada HTTP para o ALB"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTP publico - em producao prefira HTTPS/ACM"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ecs" {
  name        = "${local.name}-ecs"
  description = "Tasks ECS privadas"
  vpc_id      = aws_vpc.main.id

  dynamic "ingress" {
    for_each = local.services

    content {
      description     = "ALB para ${ingress.key}"
      from_port       = ingress.value.port
      to_port         = ingress.value.port
      protocol        = "tcp"
      security_groups = [aws_security_group.alb.id]
    }
  }

  ingress {
    description = "Comunicacao interna entre servicos"
    from_port   = 8080
    to_port     = 8082
    protocol    = "tcp"
    self        = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "db" {
  name        = "${local.name}-db"
  description = "PostgreSQL apenas a partir do ECS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "documentdb" {
  name        = "${local.name}-documentdb"
  description = "DocumentDB apenas a partir do ECS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = var.documentdb_port
    to_port         = var.documentdb_port
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "rabbitmq" {
  name        = "${local.name}-rabbitmq"
  description = "Amazon MQ RabbitMQ apenas a partir do ECS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5671
    to_port         = 5672
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "random_password" "db" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "aws_secretsmanager_secret" "db" {
  name                    = "${local.name}/database"
  kms_key_id              = aws_kms_key.main.arn
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id

  secret_string = jsonencode({
    username                   = var.db_username
    password                   = random_password.db.result
    database                   = var.db_name
    postgres_connection_string = "Host=${aws_db_instance.main.address};Port=${aws_db_instance.main.port};Database=${var.db_name};Username=${var.db_username};Password=${random_password.db.result}"
  })
}

resource "random_password" "documentdb" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "aws_secretsmanager_secret" "documentdb" {
  name                    = "${local.name}/documentdb"
  kms_key_id              = aws_kms_key.main.arn
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "documentdb" {
  secret_id = aws_secretsmanager_secret.documentdb.id

  secret_string = jsonencode({
    username          = var.documentdb_username
    password          = random_password.documentdb.result
    database          = var.documentdb_name
    connection_string = "mongodb://${var.documentdb_username}:${random_password.documentdb.result}@${aws_docdb_cluster.main.endpoint}:${var.documentdb_port}/${var.documentdb_name}?tls=true&replicaSet=rs0&readPreference=secondaryPreferred&retryWrites=false"
  })
}

resource "random_password" "rabbitmq" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "aws_secretsmanager_secret" "rabbitmq" {
  name                    = "${local.name}/rabbitmq"
  kms_key_id              = aws_kms_key.main.arn
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "rabbitmq" {
  secret_id = aws_secretsmanager_secret.rabbitmq.id

  secret_string = jsonencode({
    username = var.rabbitmq_username
    password = random_password.rabbitmq.result
  })
}

resource "aws_secretsmanager_secret" "app" {
  name                    = "${local.name}/application"
  kms_key_id              = aws_kms_key.main.arn
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id = aws_secretsmanager_secret.app.id

  secret_string = jsonencode({ api_key = var.internal_api_key })
}
