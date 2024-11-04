package knucklebone;

import java.sql.SQLException;
import java.util.ArrayList;

public class MemberDAO extends DBConnectionMgr {

	String sql;

	// 회원 가입
	public int signinMember(String nickname, String password) {
		connect();
		psmt = null;
		rs = null;

		sql = "INSERT INTO member(nickname, password) VALUES(?,?)";

		try {
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, nickname);
			psmt.setString(2, password);
			return psmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			disconnect();
		}

		return -1; // 데이터 베이스 오류
	}

	// 닉네임 중복 검사
	public boolean nicknameCheck(String nickname) {
		connect();
		psmt = null;
		rs = null;
		sql = "SELECT * FROM MEMBER WHERE nickname = ?";

		try {
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, nickname);
			rs = psmt.executeQuery();

			if (rs.next()) {
				return false; // 이미 존재하는 회원
			} else {
				return true; // 가입 가능한 회원
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
		return false;
	}

	// 로그인
	public MemberVO loginMember(String nickname, String password) {
		try {
			connect();
			sql = "SELECT nickname, point FROM member WHERE nickname = ? AND password = ?";
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, nickname);
			psmt.setString(2, password);
			rs = psmt.executeQuery();

			if (rs.next()) {
				MemberVO vo = new MemberVO();
				vo.setNickname(rs.getString("nickname"));
				vo.setPoint(rs.getString("point"));
				return vo;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace(); // 예외 정보를 출력합니다.
		} finally {
			disconnect();
		}
		return null;
	}

	// 점수 업데이트
	public boolean updatePoint(String nickname, int point) {
		try {
			connect();
			sql = "UPDATE member set point = ? where nickname = ?";
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, point + "");
			psmt.setString(2, nickname);
			psmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
		return false;
	}

	// 회원 랭크 조회
	public ArrayList<MemberVO> loadRankList() {
		ArrayList<MemberVO> rankList = new ArrayList<MemberVO>();
		sql = "SELECT *\r\n"
				+ "FROM (SELECT RANK() OVER (ORDER BY point DESC) AS rank, nickname, point\r\n"
				+ "      FROM member)\r\n"
				+ "WHERE ROWNUM <= 5";
		try {
			connect();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				MemberVO vo = new MemberVO();
				vo.setRank(rs.getString("rank"));
				vo.setNickname(rs.getString("nickname"));
				vo.setPoint(rs.getString("point"));
				rankList.add(vo);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			disconnect();
		}
		return rankList;

	}
}
