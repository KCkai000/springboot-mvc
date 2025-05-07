package com.example.demo.controller;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.tags.shaded.org.apache.regexp.recompile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.BMI;
import com.example.demo.response.ApiResponse;


@RestController  //免去撰寫 @ResponseBody 但若要透過 jsp 渲染則不適用
@RequestMapping("/api")  //以下路徑統一都有 url 前綴 "/api"
public class ApiController {
	
	
	/* *
	 * 1.首頁
	 * 路徑： /home
	 * 路徑： /
	 * 網址： http://localhost:8080/api/home
	 * 網址： http://localhost:8080/api/
	 * */
	
	@GetMapping(value = {"/home", "/"})
	public String home() {
		return "我是首頁";
	}
	/* *
	 * 2. ?帶參數
	 * 路徑： /greet?name=John&age=18
	 * 路徑： /greet?name=Mary
	 * 網址： http://localhost:8080/greet?name=John&age=18
	 * 結果： Hi John, 18(成年)
	 * 網址： http://localhost:8080/greet?name=Mary
	 * 結果： Hi Mary, 0(未成年)
	 * 限制： name參數一訂要加, age為可選參數(有初始值 0)	
	 * */
	
//	@GetMapping("/greet")
//	public String greet(@RequestParam(value = "name", required = true) String username,
//						@RequestParam(value = "age", required = false, defaultValue = "0")  Integer userage){
//		String result = String.format("Hi %s %d(%s)",
//				username, userage, userage>=18 ?"成年":"未成年");
//						return result;
//						
//
//	}
	@GetMapping("/greet")
	public String greet(@RequestParam(value = "name", required = true) String username,
						@RequestParam(value = "age", required = false, defaultValue = "0") Integer userage) {
		String result = String.format("Hi %s %d (%s)", 
				username, userage, userage >= 18 ? "成年" : "未成年");
		return result;
	}
		
		
	
	
	// 上述 2 的精簡寫法
	//方法參數名與請求參數名相同
	@GetMapping("/greet2")
	public String greet2(@RequestParam String name,
						@RequestParam(defaultValue = "0") Integer age) {
		
		String result = String.format("Hi %s %d (%s)", 
				name, age, age >= 18 ? "成年" : "未成年");
		return result;
	}
	
	/**
	 * 4. Lab 練習 I
	 * 路徑: /bmi?h=170&w=60
	 * 網址: http://localhost:8080/api/bmi?h=170&w=60
	 * 執行結果: bmi = 20.76
	 * */
//	@GetMapping("/bmi")
//	public String bmi(@RequestParam Double h,
//					  @RequestParam Double w) {
//		Double bmiValue =w/ Math.pow((h/100), 2);
//		String result = String.format("身高:%s 體重:%s BMI:%.2f",h, w, bmiValue);
//		return result;
//	}
	@GetMapping(value = "/bmi", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<BMI>> calcBmi(@RequestParam (required = false) Double h, 
						  @RequestParam (required = false) Double w) {
		if(h == null || w == null) {
//			return 
//					"""
//				{
//				"status":400,
//					"message": "提供身高(h)或體重(w)",
//					"data":	null
//				}
//				""";
			return ResponseEntity.badRequest().body(ApiResponse.error("請提供身高(h)或體重(w)"));
					//ApiResponse.error(400, );
		}
		
		double bmi = w / Math.pow(h/100, 2);
//		return """
//				{
//					"status":200,
//					"message": "BMI計算成功",
//					"data":{					
//						"height": %.1f,
//						"weight": %.1f,
//						"bmi": %.2f
//					}
//				}				
//				""".formatted(h, w, bmi);
		return ResponseEntity.ok(ApiResponse.success("BMI 計算成功", new BMI(h, w, bmi)));	
	}
	/**
	 * 5. 同名多筆資料
	 * 路徑: /age?age=17&age=21&age=20
	 * 網址: http://localhost:8080/api/age?age=17&age=21&age=20
	 * 請計算出平均年齡
	 * */
	@GetMapping(value = "/age", produces="application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getAverage(@RequestParam(name = "age", required = false) List<String> ages){
		if(ages == null || ages.size() ==0) {
			return ResponseEntity.badRequest().body(ApiResponse.error("請輸入年齡"));
		}
		double avg =ages.stream().mapToInt(Integer::parseInt).average().orElseGet(( )->0);
		Object map = Map.of("年齡", ages, "平均年齡", String.format("%.1f",avg));
		return ResponseEntity.ok(ApiResponse.success("計畫成功", map));
	}
	
	/*
	 * 6. Lab 練習: 得到多筆 score 資料
	 * 路徑: "/exam?score=80&score=100&score=50&score=70&score=30"
	 * 網址: http://localhost:8080/api/exam?score=80&score=100&score=50&score=70&score=30
	 * 請自行設計一個方法，此方法可以
	 * 印出: 最高分=?、最低分=?、平均=?、總分=?、及格分數列出=?、不及格分數列出=?
	 */
	@GetMapping(value= ("/exam"), produces="application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getScore(@RequestParam(name = "score", required = false) List<Integer> scores){
//		if(scores == null || scores.size() ==0) {
//			return ResponseEntity.badRequest().body(ApiResponse.error("請輸入成績"));
//		}
		//統計資料
		IntSummaryStatistics stat = scores.stream().mapToInt(Integer::intValue).summaryStatistics();
		//利用 Collectors.partitioningBy 分組
		//key=true 及格分數 | key=false 不及格分數
		Map<Boolean, List<Integer>> resultMap = scores.stream()
				.collect(Collectors.partitioningBy(score ->score>=60));
		Object data = Map.of(
				"最高分", stat.getMax(),
				"最低分", stat.getMin(),
				"平均", stat.getAverage(),
				"總分", stat.getSum(),
				"及格", resultMap.get(true),
				"不及格", resultMap.get(false)
				);
		return ResponseEntity.ok(ApiResponse.success("計算成功", data));
		
	}
	
}
