##############################################################################
# RIMS – Terraform Variables
##############################################################################

variable "aws_region" {
  description = "AWS region to deploy to"
  type        = string
  default     = "ap-south-1"  # Mumbai – closest to XYZ Retail's India operations
}

variable "project" {
  description = "Project name prefix used for all resource names"
  type        = string
  default     = "rims"
}

variable "environment" {
  description = "Deployment environment: dev | staging | prod"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "environment must be one of: dev, staging, prod"
  }
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"  # Override to db.t3.medium for staging/prod
}

variable "db_password" {
  description = "PostgreSQL master password — pass via TF_VAR_db_password env variable"
  type        = string
  sensitive   = true
}

variable "backend_image" {
  description = "Docker image URI for the Spring Boot backend (e.g. ECR URL)"
  type        = string
  default     = "rims-backend:latest"
}

variable "frontend_image" {
  description = "Docker image URI for the Angular/nginx frontend"
  type        = string
  default     = "rims-frontend:latest"
}

variable "backend_cpu" {
  description = "Fargate task CPU units for the backend (256 = 0.25 vCPU)"
  type        = number
  default     = 512
}

variable "backend_memory" {
  description = "Fargate task memory (MiB) for the backend"
  type        = number
  default     = 1024
}
