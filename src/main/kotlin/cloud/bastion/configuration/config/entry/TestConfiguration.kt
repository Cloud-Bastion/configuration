package cloud.bastion.configuration.config.entry

import cloud.bastion.configuration.config.Configuration
import cloud.bastion.configuration.config.type.BastionConfigType
import cloud.bastion.configuration.config.type.JsonConfigType
import com.google.inject.Singleton

@Singleton
@Configuration(
    filename = "test",
    type = JsonConfigType::class,
    path = "config/"
)
class TestConfiguration : BastionConfigType() {
    var teststring: String? = null
}