package cloud.bastion.configuration

import cloud.bastion.configuration.config.procedure.ConfigRegistrationProcedure
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfigurationTest {
    private lateinit var configRegisterProcedure: ConfigRegistrationProcedure

    @BeforeEach
    fun setUp() {
        configRegisterProcedure = ConfigRegistrationProcedure()
    }

    @Test
    fun `test loadAllConfigurations`() {
        //TODO

    }

}