package com.cos.book.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import com.cos.book.domain.Book;
import com.cos.book.domain.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 통합 테스트 (모든 Bean들을 똑같이 IoC 올리고 테스트 하는 것.)
 * WebEnvironment.MOCK 실제 톰켓을 올리는게 아니라 다른 톰켓으로 테스트
 * WebEnvironment.RANDOM_PORT 실제톰켓으로 테스트
 * @AutoConfigureMockMvc 를 IoC에 등록해줌
 * @Transactional은 각각의 테스트함수가 종료될 때마다 트랜잭션을 rollback해주는 어노테이션!!
 */
@Slf4j
@Transactional
@AutoConfigureMockMvc
//MOCK 실제 톰켓을 올리는게 아니라 다른 톰켓으로 테스트
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)	//통합테스트할때 붙혀씀
public class BookControllerIntegreTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	public void init() {
		entityManager.createNativeQuery("ALTER TABLE book ALTER COLUMN id RESTART WITH 1").executeUpdate();
	}
	@Test
	public void save_테스트() throws Exception {
		// 통합테스트이니깐 스텁이 필요없음 실제 서비스가 실행되기 때문이다.
		// given (테스트를 하기 위한 준비) -> save를 하기 위해서 준비함 (준비단계)
		Book book = new Book(null, "스프링따라하기", "코스");
		String content = new ObjectMapper().writeValueAsString(book); // json데이터를string으로 바꿔줌
		
		
		// when(테스트 실행)
		ResultActions resultAction = mockMvc.perform(post("/book")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(content)
				.accept(MediaType.APPLICATION_JSON_UTF8));
		// then (검증)
		resultAction
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.title").value("스프링따라하기")) // $ 모든 값을 나타낸다
			.andDo(MockMvcResultHandlers.print());
	}
	@Test
	public void findAll_테스트() throws Exception {
		// given
		List<Book> books = new ArrayList<>();
		books.add(new Book(null, "스프링따라하기", "코스"));
		books.add(new Book(null, "리액트따라하기", "코스"));
		books.add(new Book(null, "JUnit따라하기", "코스"));
		bookRepository.saveAll(books);
		
		// when
		ResultActions resultActions = mockMvc.perform(get("/book")
				.accept(MediaType.APPLICATION_JSON_UTF8));
		
		// 문서에서 mockMvc를 찾아보자
		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", Matchers.hasSize(3)))
			.andExpect(jsonPath("$.[0].title").value("스프링따라하기"))
			.andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void findById_테스트() throws Exception {
		// given
		Long id = 1L;
		
		List<Book> books = new ArrayList<>();
		books.add(new Book(null, "스프링따라하기", "코스"));
		books.add(new Book(null, "리액트따라하기", "코스"));
		books.add(new Book(null, "JUnit따라하기", "코스"));
		bookRepository.saveAll(books);
		
		// when
		ResultActions resultActions = mockMvc.perform(get("/book/{id}", id)
				.accept(MediaType.APPLICATION_JSON_UTF8));
		
		// then
		resultActions
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.title").value("스프링따라하기"))
		.andDo(MockMvcResultHandlers.print());
	}
	
	@Test
	public void update_테스트() throws Exception {
		// given
		Long id = 1L;
		
		List<Book> books = new ArrayList<>();
		books.add(new Book(null, "스프링따라하기", "코스"));
		books.add(new Book(null, "리액트따라하기", "코스"));
		books.add(new Book(null, "JUnit따라하기", "코스"));
		bookRepository.saveAll(books);
		
		Book book = new Book(null, "C++ 따라하기", "코스");
		String content = new ObjectMapper().writeValueAsString(book); 
		
		// when
		ResultActions resultAction = mockMvc.perform(put("/book/{id}",id)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(content)
				.accept(MediaType.APPLICATION_JSON_UTF8));
		
		// then
		resultAction
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.title").value("C++ 따라하기"))
			.andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void delete_테스트() throws Exception {
		// given
		Long id = 1L;
		
		// when
		ResultActions resultAction = mockMvc.perform(delete("/book/{id}",id)
				.accept(MediaType.TEXT_PLAIN));
		
		// then
		resultAction
			.andExpect(status().isOk())
			.andDo(MockMvcResultHandlers.print());
		
		MvcResult requestResult = resultAction.andReturn();
		String result = requestResult.getResponse().getContentAsString();
		
		assertEquals("ok", result);
	}
	
}
