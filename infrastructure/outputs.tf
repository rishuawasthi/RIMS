##############################################################################
# RIMS – Terraform Outputs
##############################################################################

output "alb_dns_name" {
  description = "Public DNS of the Application Load Balancer"
  value       = aws_lb.rims.dns_name
}

output "rds_endpoint" {
  description = "RDS PostgreSQL connection endpoint (host:port)"
  value       = "${aws_db_instance.rims.address}:${aws_db_instance.rims.port}"
  sensitive   = true
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.rims.name
}

output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "Private subnet IDs used by ECS and RDS"
  value       = module.vpc.private_subnets
}

output "public_subnet_ids" {
  description = "Public subnet IDs used by the ALB"
  value       = module.vpc.public_subnets
}
