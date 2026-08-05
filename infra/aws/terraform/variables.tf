variable "aws_region" {
  description = "Regiao AWS."
  type        = string
  default     = "sa-east-1"
}

variable "project_name" {
  description = "Nome curto do projeto."
  type        = string
  default     = "payments"
}

variable "environment" {
  description = "Ambiente."
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  type    = string
  default = "10.40.0.0/16"
}

variable "container_images" {
  description = "Imagens Docker por servico."
  type        = map(string)
  default = {
    "payment-orchestrator-java" = "public.ecr.aws/docker/library/nginx:1.27-alpine"
    "pix-boleto-kotlin"         = "public.ecr.aws/docker/library/nginx:1.27-alpine"
    "card-payment-csharp"       = "public.ecr.aws/docker/library/nginx:1.27-alpine"
  }
}

variable "desired_counts" {
  type = map(number)
  default = {
    "payment-orchestrator-java" = 2
    "pix-boleto-kotlin"         = 1
    "card-payment-csharp"       = 1
  }
}

variable "db_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "db_name" {
  type    = string
  default = "payments"
}

variable "db_username" {
  type    = string
  default = "payments_admin"
}

variable "documentdb_instance_class" {
  description = "Classe da instancia DocumentDB para dados operacionais."
  type        = string
  default     = "db.t4g.medium"
}

variable "documentdb_instance_count" {
  type    = number
  default = 1
}

variable "documentdb_name" {
  type    = string
  default = "payment_operational"
}

variable "documentdb_username" {
  type    = string
  default = "payments_docdb"
}

variable "documentdb_port" {
  type    = number
  default = 27017
}

variable "rabbitmq_instance_type" {
  type    = string
  default = "mq.t3.micro"
}

variable "rabbitmq_engine_version" {
  type    = string
  default = "3.13"
}

variable "rabbitmq_username" {
  type    = string
  default = "payments_rabbit"
}

variable "rabbitmq_port" {
  description = "Porta TLS do endpoint AMQPS."
  type        = number
  default     = 5671
}

variable "internal_api_key" {
  description = "Chave compartilhada entre os endpoints internos da PoC."
  type        = string
  sensitive   = true
  default     = "change-me-in-secrets-manager"
}

variable "alert_email" {
  description = "E-mail opcional para alarmes."
  type        = string
  default     = ""
}

variable "enable_nat_gateway" {
  description = "NAT Gateway gera custo."
  type        = bool
  default     = true
}
