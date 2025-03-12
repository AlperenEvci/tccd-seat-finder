package com.ilerijava.tcdd.entegration.entity;

import lombok.Data;
@Data
public class PassengerTypeCount {
	private int id;
    private int count;

    public PassengerTypeCount(int id, int count) {
        this.id = id;
        this.count = count;
    }
}
