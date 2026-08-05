output "alb_url" {
  value = "http://${aws_lb.main.dns_name}"
}

output "ecr_repositories" {
  value = {
    for key, repository in aws_ecr_repository.service : key => repository.repository_url
  }
}

output "rds_endpoint" {
  value = aws_db_instance.main.address
}

output "rds_port" {
  value = aws_db_instance.main.port
}

output "documentdb_endpoint" {
  value = aws_docdb_cluster.main.endpoint
}

output "documentdb_port" {
  value = aws_docdb_cluster.main.port
}

output "rabbitmq_endpoint" {
  value = aws_mq_broker.main.instances[0].endpoints[0]
}

output "database_secret_arn" {
  value = aws_secretsmanager_secret.db.arn
}

output "documentdb_secret_arn" {
  value = aws_secretsmanager_secret.documentdb.arn
}

output "rabbitmq_secret_arn" {
  value = aws_secretsmanager_secret.rabbitmq.arn
}
