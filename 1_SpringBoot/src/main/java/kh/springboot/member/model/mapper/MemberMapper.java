package kh.springboot.member.model.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;

import kh.springboot.member.model.vo.Member;
import kh.springboot.member.model.vo.TodoList;

@Mapper	// interface자체를 mapper로 연결 가능
// -> 해당 Mapper의 풀네임이 mapper의 namespace
// -> 추상 메소드의 메소드 명이 쿼리의 id
public interface MemberMapper {

	Member login(Member m);

	int insertMember(Member m);

	ArrayList<HashMap<String, Object>> selectMyList(String id);

	int updateMember(Member m);

	int updatePwd(HashMap<String, String> map);

	int deleteMember(String id);

	int checkId(String id);

	int checkNickName(String nickname);

	int checkValue(HashMap<String, String> map);

	int updateProfile(HashMap<String, String> map);

//	String findId(Member m);

//	Member findPwd(Member m);

	Member findInfo(Member m);

	ArrayList<TodoList> selectTodoList(String id);

	int insertTodo(TodoList todo);

	int updateTodo(TodoList todo);

	int deleteTodo(int num);

	ArrayList<Member> selectMembers();

	int updateMemberItem(HashMap<String, String> map);


}
