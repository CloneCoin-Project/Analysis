package com.cloneCoin.analysis.config;

import com.cloneCoin.analysis.config.bithumb.HttpRequest;
import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactoryProvider;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@Configuration
//@EnableTransactionManagement
public class DatabaseConfiguration extends AbstractR2dbcConfiguration {

    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:mysql://root:1234@localhost:3306/r2test?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Seoul");
    }

    @Bean
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory){
        return new R2dbcTransactionManager(connectionFactory);
    }


    /** Create leaders DB
     *
     create table leaders (
     leader_id bigint not null auto_increment,
     api_key varchar(255),
     last_trans_time bigint,
     secret_key varchar(255),
     total_krw double precision,
     user_id bigint UNIQUE,
     primary key (leader_id)
     ) engine=InnoDB;

     Create coins DB

     create table coins (
     coin_id bigint not null auto_increment,
     avg_price double precision,
     coin_name varchar(255),
     coin_quantity double precision,
     leader_id bigint,
     primary key (coin_id),
     foreign key(leader_id) references leaders(user_id)
     match simple on update no action on delete no action) engine=InnoDB;
     */

}
