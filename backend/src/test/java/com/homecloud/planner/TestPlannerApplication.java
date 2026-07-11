package com.homecloud.planner;

import org.springframework.boot.SpringApplication;

public class TestPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.from(PlannerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
