package knucklebone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class KnuckleboneGame extends JFrame {

	/*
	 * 필드
	 */
	Random random = new Random();

	private MemberVO vo = new MemberVO();
	private MemberDAO dao = new MemberDAO();
	private String nickname;
	private int point;
	private int bettingPoint;

	private JPanel memPanel;
	private JPanel mainPanel;
	private JPanel gamePanel;
	private JPanel moveButtonPanel;
	private JPanel rankPanel;

	// 시작 화면 필드
	private JLabel noticeLabel; // 유효성 검사 결과
	private JTextField nicknameField;
	private JPasswordField passwordField;
	private String password; // char[] -> String 변환값
	private JButton signUpButton;
	private JButton loginButton;

	// 메인 화면 필드
	private JLabel nicknameLabel;
	private JLabel pointLabel;
	private JLabel bettingNoticeLabel;
	private JTextField bettingTextField;

	// 게임 화면 필드
	private JButton[][] board1; // 플레이어1 보드
	private JButton[][] board2; // 플레이어2 보드
	private int[][] scoreBoard1 = new int[4][3]; // 플레이어1 점수판
	private int[][] scoreBoard2 = new int[4][3]; // 플레이어2 점수판
	private JLabel statusLabel; // 게임 안내 문구
	private JLabel diceLabel; // 주사위 숫자 표시
	private JLabel scoreLabel1; // 플레이어1 점수
	private JLabel scoreLabel2; // 플레이어2 점수
	private JLabel player1NameLabel; // 플레이어1 이름표
	private JLabel player2NameLabel; // 플레이어2 이름표
	private JButton rollButton; // 주사위 굴리기 버튼
	private int diceNumber; // 주사위 숫자
	private JButton backButton;
	private JButton restartButton;

	// 랭크 화면 필드
	private JPanel rankListPanel;
	private ArrayList<MemberVO> rankList; // 랭크 회원 리스트

	// 로직 필드
	private boolean isBetting;
	private boolean isTurnSet = false; // 차례를 정했는지 여부
	private boolean player1Turn; // 플레이어1 차례인지 여부
	private boolean isDicePlaced = true; // 플레이어가 주사위를 배치했는지 여부
	private boolean isDiceRolled = false; // 플레이어가 주사위를 굴렸는지 여부

	// GUI 관련 필드
	private final String bitbit = "던파 비트비트체 v2";
	private final String dosP = "DOSPilgi";
	private final String pf = "PF스타더스트";
	private final Color backColor = new Color(255, 255, 255);
	private final Color pointBackColor = new Color(253, 230, 1);
	private final Color mainColor = new Color(50, 83, 190);
	private final Color pointColor = new Color(255, 82, 174);

	/*
	 * 화면 구성
	 */

	public KnuckleboneGame() {
		setTitle("Knucklebone Game");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout(10, 10)); // 여백을 10픽셀로 설정

		/*
		 * 시작 화면
		 */
		memPanel = new JPanel();
		memPanel.setLayout(new BorderLayout());
		memPanel.setBackground(backColor);

		// 타이틀
		JLabel titleLabel = new JLabel("주사위 너클본 게임");
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font(bitbit, Font.PLAIN, 36));
		titleLabel.setForeground(pointColor);

		// 안내 문구
		noticeLabel = new JLabel("회원가입 & 로그인 후 이용 가능!");
		noticeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noticeLabel.setFont(new Font(dosP, Font.PLAIN, 14));
		noticeLabel.setForeground(mainColor);

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
		inputPanel.setBackground(backColor);
		JLabel nicknameInputLabel = new JLabel("닉네임");
		nicknameInputLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nicknameField = new JTextField();
		JLabel passwordInputLabel = new JLabel("비밀번호");
		passwordInputLabel.setHorizontalAlignment(SwingConstants.CENTER);
		passwordField = new JPasswordField();
		signUpButton = new JButton("회원가입");
		// 회원가입 버튼 클릭 이벤트
		signUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!validateInput()) {
					return;
				}
				if (checkNickname()) {
					dao.signinMember(nicknameField.getText(), password);
					noticeLabel.setText("회원가입 성공!");
				}
			}
		});
		loginButton = new JButton("로그인");
		// 로그인 버튼 클릭 이벤트
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!validateInput()) {
					return;
				}
				if (!validateLogin()) {
					return;
				}
				moveMainPanel();
			}
		});
		inputPanel.add(nicknameInputLabel);
		inputPanel.add(nicknameField);
		inputPanel.add(passwordInputLabel);
		inputPanel.add(passwordField);
		inputPanel.add(signUpButton);
		inputPanel.add(loginButton);
		inputPanel.setBorder(new EmptyBorder(75, 50, 75, 50));

		// 배치
		memPanel.add(titleLabel, BorderLayout.NORTH);
		memPanel.add(inputPanel, BorderLayout.CENTER);
		memPanel.add(noticeLabel, BorderLayout.SOUTH);
		memPanel.setBorder(new EmptyBorder(100, 100, 125, 100));

		/*
		 * 메인 화면
		 */
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(backColor);

		// 유저 정보 패널
		JPanel memberPanel = new JPanel();
		memberPanel.setLayout(new GridLayout(2, 1));
		memberPanel.setBackground(backColor);
		nicknameLabel = new JLabel(nickname);
		nicknameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nicknameLabel.setFont(new Font(bitbit, Font.PLAIN, 32));
		nicknameLabel.setForeground(pointColor);
		pointLabel = new JLabel("POINT : " + point);
		pointLabel.setHorizontalAlignment(SwingConstants.CENTER);
		pointLabel.setFont(new Font(pf, Font.PLAIN, 18));
		pointLabel.setForeground(mainColor);
		memberPanel.add(nicknameLabel);
		memberPanel.add(pointLabel);

		// 베팅 입력 패널
		JPanel gamblingPanel = new JPanel();
		gamblingPanel.setLayout(new GridLayout(3, 1, 10, 10));
		gamblingPanel.setBackground(backColor);
		JLabel bettingInfoLabel = new JLabel("▼ 배팅할 포인트 ▼");
		bettingInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bettingInfoLabel.setFont(new Font(pf, Font.PLAIN, 18));
		bettingInfoLabel.setForeground(mainColor);
		bettingNoticeLabel = new JLabel("숫자만 입력하세요!");
		bettingNoticeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bettingNoticeLabel.setFont(new Font(pf, Font.PLAIN, 14));
		bettingNoticeLabel.setBackground(pointColor);
		bettingTextField = new JTextField();
		gamblingPanel.add(bettingInfoLabel);
		gamblingPanel.add(bettingNoticeLabel);
		gamblingPanel.add(bettingTextField);
		gamblingPanel.setBorder(new EmptyBorder(80, 70, 10, 70));

		// 메인 화면 버튼 패널
		JPanel mainButtonPanel = new JPanel();
		mainButtonPanel.setLayout(new GridLayout(4, 2, 10, 10));
		mainButtonPanel.setBackground(backColor);
		JButton normalGameButton = new JButton("일반게임");
		// 일반게임 버튼 클릭
		normalGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bettingPoint = 5;
				isBetting = false;
				moveGamePanel();
			}
		});
		JButton bettingGameButton = new JButton("베팅게임");

		// 배팅게임 버튼 클릭
		bettingGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 배팅포인트 유효성 검사
				if (!validateBetPoint()) {
					return;
				}
				bettingPoint = Integer.parseInt(bettingTextField.getText());
				isBetting = true;
				moveGamePanel();
			}
		});
		JLabel normalGameLabel = new JLabel("승리시 +5p");
		normalGameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		normalGameLabel.setFont(new Font(pf, Font.PLAIN, 14));
		JLabel bettingGameLabel = new JLabel("승리시 +배팅 포인트X2");
		bettingGameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bettingGameLabel.setFont(new Font(pf, Font.PLAIN, 14));
		JButton rankButton = new JButton("랭킹");
		// 랭킹 버튼 클릭
		rankButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRankPanel();
			}
		});
		JButton logoutButton = new JButton("로그아웃");
		// 로그아웃 버튼 클릭
		logoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nickname = null;
				point = 0;
				moveMemPanel();
			}
		});
		mainButtonPanel.add(normalGameButton);
		mainButtonPanel.add(bettingGameButton);
		mainButtonPanel.add(normalGameLabel);
		mainButtonPanel.add(bettingGameLabel);
		mainButtonPanel.add(new JLabel());
		mainButtonPanel.add(new JLabel());
		mainButtonPanel.add(rankButton);
		mainButtonPanel.add(logoutButton);
		mainButtonPanel.setBorder(new EmptyBorder(10, 30, 30, 30));

		// 화면 배치
		mainPanel.add(memberPanel, BorderLayout.NORTH);
		mainPanel.add(gamblingPanel, BorderLayout.CENTER);
		mainPanel.add(mainButtonPanel, BorderLayout.SOUTH);
		mainPanel.setBorder(new EmptyBorder(60, 100, 25, 100));

		/*
		 * 게임 화면
		 */
		gamePanel = new JPanel(new GridLayout(3, 2, 10, 10)); // 여백을 10픽셀로 설정
		gamePanel.setBackground(backColor);

		// 첫 번째 게임 보드 생성
		JPanel boardPanel1 = new JPanel(new GridLayout(3, 3, 5, 5));
		boardPanel1.setBackground(backColor);
		board1 = new JButton[3][3];
		initializeBoard(boardPanel1, board1, scoreBoard1);

		// 두 번째 게임 보드 생성
		JPanel boardPanel2 = new JPanel(new GridLayout(3, 3, 5, 5));
		boardPanel2.setBackground(backColor);
		board2 = new JButton[3][3];
		initializeBoard(boardPanel2, board2, scoreBoard2);

		// 점수 보드 초기 세팅
		initializeScoreBoard(scoreBoard1);
		initializeScoreBoard(scoreBoard2);

		// 주사위 숫자를 표시하고 주사위 굴리기 버튼을 갖는 패널 생성
		JPanel dicePanel = new JPanel(new BorderLayout());
		dicePanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // 여백 추가
		dicePanel.setBackground(backColor);
		diceLabel = new JLabel("");
		diceLabel.setHorizontalAlignment(SwingConstants.CENTER);
		diceLabel.setFont(new Font(bitbit, Font.PLAIN, 32));
		dicePanel.add(diceLabel, BorderLayout.CENTER);

		rollButton = new JButton("주사위 굴리기");
		rollButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rollDice(); // 주사위 굴리기
			}
		});
		dicePanel.add(rollButton, BorderLayout.SOUTH);

		// 게임 상태를 보여줄 라벨
		statusLabel = new JLabel("차례를 정합니다.");
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setFont(new Font(dosP, Font.PLAIN, 16));

		// 플레이어1 점수 라벨
		JPanel scorePanel1 = new JPanel(new GridLayout(2, 1));
		scorePanel1.setBackground(backColor);
		scoreLabel1 = new JLabel("0");
		scoreLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		scoreLabel1.setFont(new Font(bitbit, Font.BOLD, 32));
		player1NameLabel = new JLabel(nickname);
		player1NameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		player1NameLabel.setFont(new Font(bitbit, Font.PLAIN, 20));
		scorePanel1.add(player1NameLabel);
		scorePanel1.add(scoreLabel1);

		// 플레이어2 점수 라벨
		JPanel scorePanel2 = new JPanel(new GridLayout(2, 1));
		scorePanel2.setBackground(backColor);
		scoreLabel2 = new JLabel("0");
		scoreLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		scoreLabel2.setFont(new Font(bitbit, Font.BOLD, 32));
		player2NameLabel = new JLabel("컴퓨터");
		player2NameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		player2NameLabel.setFont(new Font(bitbit, Font.PLAIN, 20));
		scorePanel2.add(player2NameLabel);
		scorePanel2.add(scoreLabel2);

		// 패널 배치
		gamePanel.add(boardPanel1);
		gamePanel.add(scorePanel1);
		gamePanel.add(dicePanel);
		gamePanel.add(statusLabel);
		gamePanel.add(boardPanel2);
		gamePanel.add(scorePanel2);
		gamePanel.setBorder(new EmptyBorder(30, 30, 30, 30));

		// 화면이동 버튼 패널
		moveButtonPanel = new JPanel();
		moveButtonPanel.setLayout(new GridLayout(1, 2));
		moveButtonPanel.setBackground(backColor);
		backButton = new JButton("뒤로가기");
		// 뒤로가기 버튼 클릭
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initializeGameBoard();
				moveMainPanel();
			}
		});
		restartButton = new JButton("다시 시작");
		// 다시 시작 버튼 클릭
		restartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initializeGameBoard();
			}
		});
		moveButtonPanel.add(backButton);
		moveButtonPanel.add(restartButton);

		/*
		 * 랭킹 화면
		 */
		rankPanel = new JPanel();
		rankPanel.setLayout(new BorderLayout());
		rankPanel.setBackground(backColor);
		JLabel rankTitleLabel = new JLabel("TOP 10");
		rankTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		rankTitleLabel.setFont(new Font(bitbit, Font.PLAIN, 32));
		rankTitleLabel.setForeground(mainColor);
		rankListPanel = new JPanel();
		rankListPanel.setLayout(new GridLayout(6, 3));
		rankListPanel.setBorder(new EmptyBorder(30, 0, 20, 0));
		rankListPanel.setBackground(backColor);
		JButton backButton2 = new JButton("뒤로 가기");
		// 뒤로가기 버튼 클릭
		backButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveMainPanel();
			}
		});

		// 화면 배치
		rankPanel.add(rankTitleLabel, BorderLayout.NORTH);
		rankPanel.add(rankListPanel, BorderLayout.CENTER);
		rankPanel.add(backButton2, BorderLayout.SOUTH);
		rankPanel.setBorder(new EmptyBorder(50, 100, 50, 100));

		// 시작화면 세팅
		add(memPanel, BorderLayout.CENTER);
		memPanel.setVisible(true);
		
		// 프레임 설정
		setSize(600, 600);
		setBackground(backColor);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	// 게임 보드 버튼 설정 + 클릭 이벤트
	private void initializeBoard(JPanel panel, JButton[][] board, int[][] scoreBoard) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = new JButton();
				board[i][j].setEnabled(false);
				int finalI = i;
				int finalJ = j;
				board[i][j].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						placeDiceOnBoard(board[finalI][finalJ], scoreBoard, finalI, finalJ); // 주사위 배치
					}
				});
				panel.add(board[i][j]);
			}
		}
	}

	// 게임 보드 버튼 초기화
	private void initializeButtons(JButton[][] scoreButton) {
		for (JButton[] row : scoreButton) {
			for (JButton button : row) {
				button.setText("");
			}
		}
	}

	// 점수판 초기화
	private void initializeScoreBoard(int[][] scoreBoard) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				scoreBoard[i][j] = 0;
			}
		}
		// 마지막 행은 1로 설정
		for (int i = 0; i < 3; i++) {
			scoreBoard[3][i] = 1;
		}
	}

	// 게임 화면 초기화
	private void initializeGameBoard() {
		initializeButtons(board1);
		initializeButtons(board2);
		initializeScoreBoard(scoreBoard1);
		initializeScoreBoard(scoreBoard2);
		isTurnSet = false;
		isDicePlaced = true;
		isDiceRolled = false;
		statusLabel.setText("차례를 정합니다.");
		player1NameLabel.setText(nickname);
		player2NameLabel.setText("컴퓨터");
		scoreLabel1.setText("0");
		scoreLabel2.setText("0");
		diceLabel.setText("");
		disableAllButtons(board1);
		disableAllButtons(board2);
		player1NameLabel.setForeground(pointColor);
		scoreLabel1.setForeground(Color.BLACK);
		player2NameLabel.setForeground(mainColor);
		scoreLabel2.setForeground(Color.BLACK);
		rollButton.setEnabled(true);
		restartButton.setEnabled(true);
		backButton.setEnabled(true);
	}

	// 랭크 리스트 불러오기
	private void getRankList() {
		rankListPanel.removeAll();
		rankListPanel.revalidate();
		rankListPanel.repaint();
		rankList = dao.loadRankList();
		JLabel rankLabel = new JLabel("RANK");
		rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
		rankLabel.setFont(new Font(pf, Font.PLAIN, 18));
		rankLabel.setForeground(pointBackColor);
		rankListPanel.add(rankLabel);
		JLabel nickLabel = new JLabel("NICKNAME");
		nickLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nickLabel.setFont(new Font(pf, Font.PLAIN, 18));
		nickLabel.setForeground(pointBackColor);
		rankListPanel.add(nickLabel);
		JLabel poLabel = new JLabel("POINT");
		poLabel.setHorizontalAlignment(SwingConstants.CENTER);
		poLabel.setFont(new Font(pf, Font.PLAIN, 18));
		poLabel.setForeground(pointBackColor);
		rankListPanel.add(poLabel);
		for (MemberVO user : rankList) {
			JLabel userRank = new JLabel(user.getRank());
			userRank.setHorizontalAlignment(SwingConstants.CENTER);
			userRank.setFont(new Font(pf, Font.PLAIN, 16));
			rankListPanel.add(userRank);
			JLabel userNick = new JLabel(user.getNickname());
			userNick.setHorizontalAlignment(SwingConstants.CENTER);
			userNick.setFont(new Font(pf, Font.PLAIN, 16));
			rankListPanel.add(userNick);
			JLabel userPoint = new JLabel(user.getPoint());
			userPoint.setHorizontalAlignment(SwingConstants.CENTER);
			userPoint.setFont(new Font(pf, Font.PLAIN, 16));
			rankListPanel.add(userPoint);
		}
	}

	/*
	 * 화면 이동 메소드
	 */

	// 시작 화면 이동
	private void moveMemPanel() {
		remove(mainPanel);
		remove(moveButtonPanel);
		add(memPanel, BorderLayout.CENTER);
		memPanel.setVisible(true);
		revalidate();
		repaint();
	}

	// 메인 화면 이동
	private void moveMainPanel() {
		remove(memPanel);
		remove(gamePanel);
		remove(rankPanel);
		remove(moveButtonPanel);
		add(mainPanel, BorderLayout.CENTER);
		mainPanel.setVisible(true);
		revalidate();
		repaint();
	}

	// 게임 화면 이동
	private void moveGamePanel() {
		initializeGameBoard();
		remove(mainPanel);
		add(gamePanel, BorderLayout.CENTER);
		add(moveButtonPanel, BorderLayout.SOUTH);
		gamePanel.setVisible(true);
		revalidate();
		repaint();
	}

	// 랭크 화면 이동
	private void moveRankPanel() {
		getRankList();
		remove(mainPanel);
		add(rankPanel, BorderLayout.CENTER);
		rankPanel.setVisible(true);
		revalidate();
		repaint();
	}

	/*
	 * 유효성 검사 메소드
	 */

	// 회원가입&로그인 유효성 검사
	private boolean validateInput() {
		char[] passwordChars = passwordField.getPassword();
		password = new String(passwordChars);
		if (nicknameField.getText().isEmpty()) {
			noticeLabel.setText("닉네임을 입력하세요");
			return false;
		}
		if (password.isEmpty()) {
			noticeLabel.setText("비밀번호를 입력하세요");
			return false;
		}
		return true;
	}

	// 회원가입 닉네임 중복 검사
	private boolean checkNickname() {
		if (!dao.nicknameCheck(nicknameField.getText())) {
			noticeLabel.setText("이미 존재하는 회원입니다");
			return false;
		}
		return true;
	}

	// 로그인 성공 여부
	private boolean validateLogin() {
		vo = dao.loginMember(nicknameField.getText(), password);
		if (vo == null) {
			noticeLabel.setText("닉네임과 비밀번호를 확인하세요T_T");
			return false;
		}
		nickname = vo.getNickname();
		point = Integer.parseInt(vo.getPoint());
		nicknameLabel.setText(nickname);
		player1NameLabel.setText(nickname);
		pointLabel.setText("POINT : " + point);

		return true;
	}

	// 배팅포인트 유효성 검사
	private boolean validateBetPoint() {
		// 입력했는지 검사
		if (bettingTextField.getText().isEmpty()) {
			bettingNoticeLabel.setText("배팅할 포인트를 입력하세요!");
			return false;
		}
		// 숫자 외의 문자가 들어갔는지 검사
		if (!bettingTextField.getText().matches("[0-9]+")) {
			bettingNoticeLabel.setText("숫자 외의 문자는 입력할 수 없습니다!");
			return false;
		}
		bettingPoint = Integer.parseInt(bettingTextField.getText());
		// 포인트보다 배팅포인트가 많은지 검사
		if (bettingPoint > point) {
			bettingNoticeLabel.setText("보유한 포인트보다 많습니다!");
			return false;
		}
		return true;
	}

	/*
	 * 게임 로직 & 메소드
	 */

	// 주사위 굴리기 메소드
	private void rollDice() {

		// 플레이어가 주사위를 배치하지 않았으면 주사위를 굴리지 않음
		if (!isDicePlaced)
			return;

		diceNumber = random.nextInt(6) + 1; // 주사위 숫자는 1에서 6까지
		diceLabel.setText(diceNumber + "");
//		rollEffect();
//		diceNumber = Integer.parseInt(diceLabel.getText());

		// 처음 주사위를 굴리는 거라면 차례부터 정하기
		if (!isTurnSet) {
			if (diceNumber % 2 == 0) {
				player1Turn = true;
			}
			this.switchTurn();
			isTurnSet = true;
			backButton.setEnabled(false);
			restartButton.setEnabled(false);
			return;
		}

		// 다음 행동 안내문구
		if (player1Turn) {
			statusLabel.setText("주사위를 배치하세요!");
		} else {
			statusLabel.setText("컴퓨터 주사위를 배치중···");
		}
		isDiceRolled = true;
		isDicePlaced = false;
	}

	// 주사위 배치 메소드
	private void placeDiceOnBoard(JButton button, int[][] scoreBoard, int finalI, int finalJ) {
		// 플레이어가 주사위를 굴리지 않으면 실행하지 않음
		if (!isDiceRolled)
			return;

		// 클릭한 버튼에 주사위 숫자 표시하기
		button.setText(String.valueOf(diceNumber));
		scoreBoard[finalI][finalJ] = diceNumber;

		// 상대방 보드와 비교
		compareNumber(scoreBoard2, board2, finalJ);

		// 점수 계산하기
		scoreLabel1.setText(totalScore(scoreBoard1) + "");
		scoreLabel2.setText(totalScore(scoreBoard2) + "");

		// 보드가 다 채워졌는지 확인
		if (!isBoardFull(scoreBoard1)) {
			// turn 바꾸기
			isDiceRolled = false;
			isDicePlaced = true;
			this.switchTurn();
		}
	}

	// 컴퓨터 주사위 배치 메소드
	private void ComputerplaceDice() {
		// 랜덤으로 인덱스값 뽑기
		int row = random.nextInt(3);
		int col = random.nextInt(3);

		while (!board2[row][col].getText().isEmpty()) {
			row = random.nextInt(3);
			col = random.nextInt(3);
		}

		// 버튼에 주사위 숫자 표시하기
		board2[row][col].setText(String.valueOf(diceNumber));
		scoreBoard2[row][col] = diceNumber;

		// 상대방 보드와 비교
		compareNumber(scoreBoard1, board1, col);

		// 점수 계산하기
		scoreLabel1.setText(totalScore(scoreBoard1) + "");
		scoreLabel2.setText(totalScore(scoreBoard2) + "");

		// 보드가 다 채워졌는지 확인
		if (!isBoardFull(scoreBoard2)) {
			// turn 바꾸기
			isDiceRolled = false;
			isDicePlaced = true;
			this.switchTurn();
		}

	}

	// 상대방 보드와 숫자 비교
	private void compareNumber(int[][] scoreBoard, JButton[][] board, int col) {
		for (int i = 0; i < 3; i++) {
			if (scoreBoard[i][col] == diceNumber) {
				scoreBoard[i][col] = 0;
				board[i][col].setText("");
			}
		}
	}

	// 현재 점수 반환 메소드
	private int totalScore(int[][] sB) {
		int score = 0;
		for (int j = 0; j < 3; j++) {
			// 열의 숫자가 모두 동일하면
			if (sB[0][j] != 0 && sB[0][j] == sB[1][j] && sB[0][j] == sB[2][j]) {
				sB[3][j] = 3;
			} else if (sB[0][j] != 0 && (sB[0][j] == sB[1][j] || sB[0][j] == sB[2][j])
					|| sB[1][j] != 0 && sB[1][j] == sB[2][j]) {
				sB[3][j] = 2;
			}
			score += (sB[0][j] + sB[1][j] + sB[2][j]) * sB[3][j];
		}
		return score;
	}

	// 보드가 다 채워졌는지 확인
	private boolean isBoardFull(int[][] scoreBoard) {
		for (int[] row : scoreBoard) {
			for (int num : row) {
				if (num == 0) {
					return false; // 0이 하나라도 있으면 false 반환
				}
			}
		}

		// 게임 마무리
		disableAllButtons(board1);
		disableAllButtons(board2);
		rollButton.setEnabled(false);
		statusLabel.setText("게임이 종료되었습니다!");

		// 최종 점수 계산
		int score1 = Integer.parseInt(scoreLabel1.getText());
		int score2 = Integer.parseInt(scoreLabel2.getText());
		if (score1 == score2) { // 무승부
			player1NameLabel.setText(nickname + "(DRAW)");
			player1NameLabel.setForeground(Color.GREEN);
			scoreLabel1.setForeground(Color.GREEN);
			player2NameLabel.setText("컴퓨터 (DRAW)");
			player2NameLabel.setForeground(Color.GREEN);
			scoreLabel2.setForeground(Color.GREEN);
		} else if (score1 > score2) { // player 승리
			player1NameLabel.setText(nickname + " WIN!!");
			player1NameLabel.setForeground(Color.BLUE);
			scoreLabel1.setForeground(Color.BLUE);
			player2NameLabel.setText("컴퓨터 LOSE..T_T");
			player2NameLabel.setForeground(Color.RED);
			scoreLabel2.setForeground(Color.RED);
			gainPoint();
		} else { // computer 승리
			player1NameLabel.setText(nickname + " LOSE..T_T");
			player1NameLabel.setForeground(Color.RED);
			scoreLabel1.setForeground(Color.RED);
			player2NameLabel.setText("컴퓨터 WIN!!");
			player2NameLabel.setForeground(Color.BLUE);
			scoreLabel2.setForeground(Color.BLUE);
			reducePoint();
		}

		dao.updatePoint(nickname, point);
		pointLabel.setText("POINT : " + point);
		if (!isBetting) {
			restartButton.setEnabled(true);
		}
		backButton.setEnabled(true);
		return true; // 판이 모두 채워지면 true 반환 후 게임 종료
	}

	// 점수 추가 메소드
	private void gainPoint() {
		point += bettingPoint;
	}

	// 점수 차감 메소드
	private void reducePoint() {
		if (!isBetting)
			return;
		point -= bettingPoint;
	}

	// turn 바꾸기
	private void switchTurn() {
		if (player1Turn) { // Player 1 turn
			player1Turn = false;
			disableAllButtons(board1);
			enableAllButtons(board2);
			statusLabel.setText("컴퓨터 주사위 굴리는 중···");
			// 1초 후 컴퓨터 주사위 굴리기
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					rollDice();
				}
			}, 1000);
			// 2초 후 컴퓨터 주사위 배치하기
			Timer timer2 = new Timer();
			timer2.schedule(new TimerTask() {
				@Override
				public void run() {
					ComputerplaceDice();
				}
			}, 2000);
		} else { // Computer turn
			player1Turn = true;
			disableAllButtons(board2);
			enableAllButtons(board1);
			statusLabel.setText("주사위를 굴리세요!");
		}
	}

	// 버튼 활성화 메소드
	private void enableAllButtons(JButton[][] board) {
		for (JButton[] buttons : board) {
			for (JButton button : buttons) {
				if (button.getText().isEmpty()) // 값이 있는 버튼은 제외
					button.setEnabled(true);
				else
					button.setEnabled(false);
			}
		}
	}

	// 버튼 비활성화 메소드
	private void disableAllButtons(JButton[][] board) {
		for (JButton[] buttons : board) {
			for (JButton button : buttons) {
				button.setEnabled(false);
			}
		}
	}

	// 주사위 애니메이션 메소드
//	private int time;
//	private int num;
	private void rollEffect() {
//		int cnt = random.nextInt(12) + 30;
//		num = Integer.parseInt(diceLabel.getText());
//		for (int i = 0; i < cnt; i++) {
//			num++;
//			if (num > 6)
//				num = 1;
//			Timer rollTimer = new Timer();
//			rollTimer.schedule(new TimerTask() {
//				@Override
//				public void run() {
//					diceLabel.setText(num + "");
//				}
//			}, i * 500);
//		}
//		time = 1;
//		while (time < cnt) {
//			num++;
//			if (num > 6)
//				num = 1;
//			Timer rollTimer = new Timer();
//			rollTimer.schedule(new TimerTask() {
//				@Override
//				public void run() {
//					diceLabel.setText(num + "");
//					time++;
//				}
//			}, time * 100);
//		}
	}
}
