resource "aws_db_subnet_group" "main" {
  name       = "${local.name}-db-subnets"
  subnet_ids = values(aws_subnet.private)[*].id
}

resource "aws_db_instance" "main" {
  identifier = "${local.name}-postgres"

  engine         = "postgres"
  engine_version = "16"
  instance_class = var.db_instance_class

  allocated_storage     = 20
  max_allocated_storage = 100
  storage_type          = "gp3"
  storage_encrypted     = true
  kms_key_id            = aws_kms_key.main.arn

  db_name  = var.db_name
  username = var.db_username
  password = random_password.db.result
  port     = 5432

  multi_az               = false
  publicly_accessible    = false
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.db.id]

  backup_retention_period = 7
  deletion_protection     = var.environment == "prod"
  skip_final_snapshot     = var.environment != "prod"
  apply_immediately       = true

  performance_insights_enabled    = true
  performance_insights_kms_key_id = aws_kms_key.main.arn
  monitoring_interval             = 0
  auto_minor_version_upgrade      = true

  tags = { Name = "${local.name}-postgres" }
}
