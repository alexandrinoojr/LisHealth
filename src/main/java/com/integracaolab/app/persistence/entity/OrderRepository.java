package com.integracaolab.app.persistence.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import com.integracaolab.app.persistence.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity,Long>{
	
}
