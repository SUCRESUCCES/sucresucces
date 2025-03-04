package kh.springboot.ajax.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import kh.springboot.board.model.service.BoardService;
import kh.springboot.board.model.vo.Board;
import kh.springboot.board.model.vo.Reply;
import kh.springboot.member.model.service.MemberService;
import kh.springboot.member.model.vo.Member;
import kh.springboot.member.model.vo.TodoList;
import lombok.RequiredArgsConstructor;

@RestController // Controller + ResponseBody
@RequestMapping({ "/member", "/board", "/admin" })
@RequiredArgsConstructor
@SessionAttributes("loginUser") // 세션 값 받기위해서
public class AjaxController {

	private final MemberService mService;
	private final JavaMailSender mailSender;
	private final BoardService bService;

	@GetMapping("checkValue")
	public int checkValue(@RequestParam("value") String value, @RequestParam("column") String column) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("column", column);
		map.put("value", value);
		int count = mService.checkValue(map);
		return count;
	}

	@PutMapping("profile")
	public int updateProfile(@RequestParam(value = "profile", required = false) MultipartFile profile, Model model) {
		// System.out.println(profile);

		Member m = (Member) model.getAttribute("loginUser");

		String savePath = "c:\\profiles";
		File folder = new File(savePath);
		if (!folder.exists())
			folder.mkdirs(); // if문이 한줄이면 중괄호 생략 가능 두줄이상부터는 중괄호 생략 불가능.. 근데 중괄호 없는 것보다 있는게 좋긴함

		if (m.getProfile() != null) {
			File f = new File(savePath + "\\" + m.getProfile());
			f.delete();
		}

		String renameFileName = null;
		if (profile != null) {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			int ranNum = (int) (Math.random() * 1000000);
			String originFileName = profile.getOriginalFilename();
			renameFileName = sdf.format(new Date()) + ranNum
					+ originFileName.substring(originFileName.lastIndexOf("."));

			try {
				profile.transferTo(new File(folder + "\\" + renameFileName));
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}

		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("id", m.getId());
		map.put("profile", renameFileName);

		int result = mService.updateProfile(map);
		if (result > 0) {
			m.setProfile(renameFileName);
			model.addAttribute("loginUser", m); // 세션 값도 같이 바꿔주는 것
		}

		return result;

		// Ensure that the compiler uses the '-parameters' flag. 이런 오류가 뜨면?

	}

	@GetMapping("echeck")
	public String checkEmail(@RequestParam("email") String email) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();

		String subject = "[SpringBoot] 이메일 확인";
		String body = "<h1 align='center'>SpringBoot 이메일 확인</h1><br>";
		body += "<div style='border: 3px solid green; text-align: center; font-size: 15px;'>본 메일은 이메일을 확인하기 위해 발송되었습니다.<br>";
		body += "아래 숫자를 인증번호 확인란에 작성하여 확인해주시기 바랍니다.<br><br>";

		String random = "";
		for (int i = 0; i < 5; i++) {
			random += (int) (Math.random() * 10);
		}

		body += "<span style='font-size: 30px; text-decoration: underline;'><b>" + random + "</b></span><br></div>";

		MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
		try {
			mimeMessageHelper.setTo(email);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setText(body, true);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		mailSender.send(mimeMessage);

		return random;
	}

	@PostMapping("list")
	public int insertTodo(@ModelAttribute TodoList todo) {
		return mService.insertTodo(todo);
	}

	@PutMapping("list")
	public int updateTodo(@ModelAttribute TodoList todo) {
		return mService.updateTodo(todo);
	}

	@DeleteMapping("list")
	public int deleteTodo(@RequestParam("num") int num) {
		return mService.deleteTodo(num);
	}

	@GetMapping("top")
	public ArrayList<Board> selectTop(HttpServletResponse response) {
		// HttpMessageConverter
		// 기본 문자 : StringHttpMessageConverter
		// 기본 객체 : MappingJackson2HttpMessageConverter -> application/json
		ArrayList<Board> list = bService.selectTop();
		return list;
	}

	@PostMapping(value = "reply")
	public ArrayList<Reply> insertReply(@ModelAttribute Reply r) {
		// 저장
		int result = bService.insertReply(r);
		// 가져오기
		ArrayList<Reply> list = bService.selectReplyList(r.getRefBoardId());

		ObjectMapper om = new ObjectMapper();

		return list;
	}

	@DeleteMapping("reply")
	public int deleteReply(@RequestParam("rId") int rId) {
		return bService.deleteReply(rId);

	}

	@PutMapping("reply")
	public int updateReply(@ModelAttribute Reply r) {
		return bService.updateReply(r);
	}

	@PutMapping("members")
	public int updateMember(@RequestBody HashMap<String, String> map) {	//fetch 사용해서 body를 요청하고 JSON형식(key,value)과 비슷한 map 사용
		// 위의 것을 사용해서 아래의 것 사용하지 않아도 됨
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("val", value);
//		map.put("col", column);
//		map.put("id", id);
		System.out.println(map);
		
		if(map.get("column").equals("NICKNAME")) {
			int count = mService.checkValue(map);
			if(count != 0) {
				return -1;
			}
		}else if(map.get("column").equals("STATUS") || map.get("column").equals("ADMIN")) {
			map.put("column", map.get("column").equals("STATUS") ? "member_status" : "is_admin");
		}
		return mService.updateMemberItem(map);
	}
	
	@PutMapping("status")
	public int updateBoardStatus(@RequestBody HashMap<String, Object> map) {
		return bService.updateBoardStatus(map);
	}
}
