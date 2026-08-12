locals {
  name = "${var.project_name}-${var.environment}"

  common_tags = {
    Project      = var.project_name
    Environment  = var.environment
    ManagedBy    = "Terraform"
    Architecture = "PaymentPoC"
  }

  azs = slice(data.aws_availability_zones.available.names, 0, 2)

  public_subnets = {
    for index, az in local.azs :
    az => cidrsubnet(var.vpc_cidr, 4, index)
  }

  private_subnets = {
    for index, az in local.azs :
    az => cidrsubnet(var.vpc_cidr, 4, index + 8)
  }

  services = {
    "payment-orchestrator-java" = {
      cpu          = 512
      memory       = 1024
      port         = 8080
      public_alb   = true
      health_path  = "/actuator/health"
      max_capacity = 4
    }
    "pix-boleto-kotlin" = {
      cpu          = 512
      memory       = 1024
      port         = 8081
      public_alb   = true
      health_path  = "/actuator/health"
      max_capacity = 4
    }
    "card-payment-csharp" = {
      cpu          = 512
      memory       = 1024
      port         = 8082
      public_alb   = true
      health_path  = "/health"
      max_capacity = 4
    }
  }

  alb_routes = {
    "payment-orchestrator-java" = ["/payments*", "/reconciliation*"]
    "pix-boleto-kotlin"         = ["/bank-rail*"]
    "card-payment-csharp"       = ["/cards*", "/webhooks*"]
  }

  rabbitmq_host = replace(
    replace(aws_mq_broker.main.instances[0].endpoints[0], "amqps://", ""),
    ":${var.rabbitmq_port}",
    ""
  )
}
