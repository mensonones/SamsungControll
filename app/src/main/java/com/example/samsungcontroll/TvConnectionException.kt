package com.example.samsungcontroll

sealed class TvConnectionException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    class NetworkUnreachable(
        val ip: String,
        cause: Throwable? = null
    ) : TvConnectionException("Host $ip está fora da rede local ou inalcançável.", cause)

    class PermissionDenied(
        val ip: String
    ) : TvConnectionException("Permissão recusada pela TV $ip. Aceite o alerta na tela da TV.")

    class DeviceNotFound(
        message: String = "Nenhuma TV Samsung foi encontrada na rede Wi-Fi."
    ) : TvConnectionException(message)

    class CertificateMismatch(
        val ip: String
    ) : TvConnectionException("Certificado da TV $ip alterado ou inválido (Falha na pinagem TOFU).")
}
