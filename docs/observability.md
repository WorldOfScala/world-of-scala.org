# Obsvervabilty 

OpenTelemetry is used to collect metrics, traces and logs.

For development purposes, the [Aspire](https://aspire.dev/) OpenTelemetry collector can be run locally with the following command:

```bash
docker-comppose -f aspire.yaml
```

Activation is triggerd by setting the environment variable `OTEL_EXPORTER_OTLP_ENDPOINT` to the endpoint of the OpenTelemetry collector.

```bash
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 code .
```

Will run local environement with OpenTelemetry enabled. 


