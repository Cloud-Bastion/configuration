package cloud.bastion.configuration.parser

@Retention(AnnotationRetention.RUNTIME)
annotation class Group(
    val groupName: String,
    val groupOrder: Int = -1
)
