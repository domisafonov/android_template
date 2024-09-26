package net.domisafonov.templateproject.data.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import net.domisafonov.templateproject.data.settings_store.proto.ProtoSettings
import java.io.InputStream
import java.io.OutputStream

const val PROTO_SETTINGS_FILENAME = "settings.pb"

object ProtoSettingsSerializer : Serializer<ProtoSettings> {

    override val defaultValue: ProtoSettings = ProtoSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoSettings = try {
        ProtoSettings.parseFrom(input)
    } catch (e: InvalidProtocolBufferException) {
        throw CorruptionException("error parsing protobuf data", e)
    }

    override suspend fun writeTo(t: ProtoSettings, output: OutputStream) {
        t.writeTo(output)
    }
}
