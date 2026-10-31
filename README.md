# Calculadora Job

### Limpieza diaria de logs de la calculadora

Este job de Kubernetes se ejecuta una vez al día, a las 00:05 UTC. Vacía el
archivo activo `api-requests.txt` para liberar espacio y crea en el mismo
directorio un archivo `api-requests-cleanup-YYYY-MM-DD.txt` con la fecha y hora
del borrado. Ambos archivos permanecen en el volumen persistente compartido con
el backend. El job no requiere servicios externos ni credenciales de AWS.

## Configuración

| Variable de entorno | Valor predeterminado | Propósito |
| --- | --- | --- |
| `REQUEST_LOG_FILE` | `/app/logs/api-requests.txt` | Ruta del archivo de logs que se vaciará |

El archivo de registro de borrado usa la fecha UTC, por ejemplo
`api-requests-cleanup-2026-10-07.txt`, y contiene la hora exacta del borrado en
formato ISO 8601.

## Kubernetes

El [CronJob](k8s/cronjob.yaml) monta el PVC `calculadora-logs` en `/app/logs`.
El backend debe montar el mismo PVC en `/app/logs` para que el job pueda limpiar
el archivo que escribe y conservar el registro del borrado:

```sh
docker build -t your-registry/calculadora-job:latest .
docker push your-registry/calculadora-job:latest
kubectl apply -f k8s/cronjob.yaml
```

Para ejecutar el job fuera de Kubernetes, monta el directorio de logs y
configura `REQUEST_LOG_FILE` con la ruta de `api-requests.txt`.

## Pruebas

```sh
./mvnw test
```
