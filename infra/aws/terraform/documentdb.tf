resource "aws_docdb_subnet_group" "main" {
  name       = "${local.name}-documentdb-subnets"
  subnet_ids = values(aws_subnet.private)[*].id

  tags = { Name = "${local.name}-documentdb-subnets" }
}

resource "aws_docdb_cluster" "main" {
  cluster_identifier      = "${local.name}-documentdb"
  engine                  = "docdb"
  engine_version          = "5.0"
  master_username         = var.documentdb_username
  master_password         = random_password.documentdb.result
  port                    = var.documentdb_port
  db_subnet_group_name    = aws_docdb_subnet_group.main.name
  vpc_security_group_ids  = [aws_security_group.documentdb.id]
  storage_encrypted       = true
  kms_key_id              = aws_kms_key.main.arn
  backup_retention_period = 7
  preferred_backup_window = "03:00-04:00"
  skip_final_snapshot     = var.environment != "prod"
  deletion_protection     = var.environment == "prod"
  apply_immediately       = true

  tags = { Name = "${local.name}-documentdb" }
}

resource "aws_docdb_cluster_instance" "main" {
  count = var.documentdb_instance_count

  identifier                 = "${local.name}-documentdb-${count.index + 1}"
  cluster_identifier         = aws_docdb_cluster.main.id
  instance_class             = var.documentdb_instance_class
  engine                     = "docdb"
  auto_minor_version_upgrade = true
  apply_immediately          = true
}
