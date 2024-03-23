package dailyquest.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    @Bean
    fun dataSource(): DataSource {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @BatchDataSource
    @Bean
    @ConfigurationProperties("spring.datasource.batch")
    fun batchDataSource(): DataSource {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }
}