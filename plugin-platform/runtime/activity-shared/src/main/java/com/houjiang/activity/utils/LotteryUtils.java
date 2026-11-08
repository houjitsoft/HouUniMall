package com.houjiang.activity.utils;

import com.houjiang.activity.dto.GiftDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Amos
 */
public class LotteryUtils {

	/**
	 * 抽奖方法
	 *
	 * @param gifts 商品中奖概率列表，保证顺序和实际物品对应
	 * @return 中奖商品索引
	 */
	public static Integer lottery(List<GiftDTO> gifts) {

		// 存储概率
		List<Double> orignalRates = new ArrayList<>(gifts.size());
		for (GiftDTO gift : gifts) {
			double probability = gift.getProbability();
			if (probability < 0) {
				probability = 0;
			}
			orignalRates.add(probability);
		}

		if (orignalRates == null || orignalRates.isEmpty()) {
			return -1;
		}

		int size = orignalRates.size();

		// 计算总概率，这样可以保证不一定总概率是1
		double sumRate = 0d;
		for (double rate : orignalRates) {
			sumRate += rate;
		}

		// 计算每个物品在总概率的基础下的概率情况
		List<Double> sortOrignalRates = new ArrayList<>(size);
		Double tempSumRate = 0d;
		for (double rate : orignalRates) {
			tempSumRate += rate;
			sortOrignalRates.add(tempSumRate / sumRate);
		}

		// 根据区块值来获取抽取到的物品索引
		double nextDouble = Math.random();
		sortOrignalRates.add(nextDouble);
		Collections.sort(sortOrignalRates);

		return sortOrignalRates.indexOf(nextDouble);
	}

	/** 本地抽奖概率自测入口，勿作为 Spring Boot 主类 */
	public static void localMain(String[] args) {
		Integer sum = 1 + 23 + 33 + 42 + 52 + 62;
		Double allRate = Double.parseDouble(sum.toString());
		List<GiftDTO> gifts = new ArrayList<>();
		gifts.add(new GiftDTO(1, "一等奖", "P1", 1 / allRate));
		gifts.add(new GiftDTO(23, "二等奖", "P2", 3 / allRate));
		gifts.add(new GiftDTO(33, "三等奖", "P3", 5 / allRate));
		gifts.add(new GiftDTO(42, "四等奖", "P4", 8 / allRate));
		gifts.add(new GiftDTO(52, "五等奖", "P5", 10 / allRate));
		gifts.add(new GiftDTO(62, "谢谢参与", "P6", 10 / allRate));

		Integer lottery = lottery(gifts);
		System.out.println("===================" + lottery);

	}

}
