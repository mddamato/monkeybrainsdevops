package com.demo.mq.consumer.info;

import java.io.Serializable;
import java.util.Date;

public class OrderInfo implements Serializable{
	private static final long serialVersionUID = 1196327608575230459L;

	private int goodsId;
	private String goods;
	private float price;
	private Date createTime;
	
	public OrderInfo(int goodsId, String goods, float price) {
		this.goodsId = goodsId;
		this.goods = goods;
		this.price = price;
		this.createTime = new Date();
	}
	
	public String getGoods() {
		return goods;
	}
	public void setGoods(String goods) {
		this.goods = goods;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(int goodsId) {
		this.goodsId = goodsId;
	}
}