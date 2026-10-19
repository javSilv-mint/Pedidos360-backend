#!/usr/bin/env bash
# Instala los 3 microservicios de Pedidos360 como servicios systemd en Amazon Linux 2023.
# Uso: editar las variables de abajo y ejecutar  ->  sudo bash instalar-ec2.sh
set -euo pipefail

# ====== EDITAR ======
RDS_ENDPOINT="pedidos360-db.xxxxxxxx.us-east-1.rds.amazonaws.com"
DB_USER="admin"
DB_PASS="CAMBIAR"
AZURE_ISSUER_URI="PEGAR-ISSUER-DEL-WELL-KNOWN"
AZURE_JWK_SET_URI="PEGAR-JWKS_URI-DEL-WELL-KNOWN"
AZURE_API_CLIENT_ID="PEGAR-API-CLIENT-ID"
CORS_ORIGINS="https://dxxxx.cloudfront.net,http://localhost:4200"
# ====================

JAR_DIR=/opt/pedidos360
ENV_DIR=/etc/pedidos360
mkdir -p "$JAR_DIR" "$ENV_DIR"
cp /home/ec2-user/ms-*-1.0.0.jar "$JAR_DIR"/
chown -R ec2-user:ec2-user "$JAR_DIR"

cat > "$ENV_DIR/comun.env" <<ENV
DB_USER=$DB_USER
DB_PASS=$DB_PASS
AZURE_ISSUER_URI=$AZURE_ISSUER_URI
AZURE_JWK_SET_URI=$AZURE_JWK_SET_URI
AZURE_API_CLIENT_ID=$AZURE_API_CLIENT_ID
CORS_ORIGINS=$CORS_ORIGINS
ENV
chmod 600 "$ENV_DIR/comun.env"

declare -A BASES=( [ms-productos]=pedidos360_productos [ms-pedidos]=pedidos360_pedidos [ms-clientes]=pedidos360_clientes )

for ms in ms-productos ms-pedidos ms-clientes; do
  db="${BASES[$ms]}"
  cat > "/etc/systemd/system/$ms.service" <<UNIT
[Unit]
Description=Pedidos360 $ms
After=network-online.target
Wants=network-online.target

[Service]
User=ec2-user
EnvironmentFile=$ENV_DIR/comun.env
Environment="DB_URL=jdbc:mysql://$RDS_ENDPOINT:3306/$db?createDatabaseIfNotExist=true&sslMode=REQUIRED&serverTimezone=America/Santiago"
ExecStart=/usr/bin/java -Xms128m -Xmx300m -jar $JAR_DIR/$ms-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
UNIT
done

systemctl daemon-reload
systemctl enable --now ms-productos ms-pedidos ms-clientes
echo "Esperando que arranquen..."
sleep 30
systemctl status ms-productos ms-pedidos ms-clientes --no-pager | grep -E "●|Active:"
echo "Logs: journalctl -u ms-productos -f"
