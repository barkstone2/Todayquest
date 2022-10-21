package todayquest;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import todayquest.common.UserLevelLock;
import todayquest.user.dto.UserPrincipal;

import javax.sql.DataSource;
import java.util.Optional;

@EnableJpaAuditing
@SpringBootApplication
public class TodayquestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodayquestApplication.class, args);
	}

	@Primary
	@Bean
	@ConfigurationProperties("spring.datasource.hikari")
	public DataSource dataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	@Bean
	@ConfigurationProperties("userlock.datasource.hikari")
	public DataSource userLockDataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	@Bean
	public UserLevelLock userLevelLock() {
		return new UserLevelLock(userLockDataSource());
	}

	@Bean
	public AuditorAware<Long> auditorProvider() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (null == authentication || !authentication.isAuthenticated()) {
			return null;
		}
		UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
		return () -> Optional.of(user.getUserId());
	}

}
