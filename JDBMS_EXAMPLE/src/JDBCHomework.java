import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.io.*;

public class JDBCHomework {
	public static void main(String args[])
			throws SQLException, IOException
			{
				try
				{
					Class.forName("********************");
					String host = "******************";
					String db= "***************";
					String user = "****************";
					String password = getPassword();
					Connection con = DriverManager.getConnection("jdbc:mysql://" + host + db + "?useSSL=false&serverTimezone=Asia/Seoul", user, password);
					//Perform query using Statement
					// by providing SSN at run time
					
					Statement stmt = con.createStatement();
					stmt.executeUpdate("drop table if exists tempssn"); // 이미 tempssn memory table이 존재한다면 제거
					stmt.executeUpdate("create temporary table tempssn(fname varchar(15), lname varchar(15), ssn char(9) primary key, superssn char(9)) engine=MEMORY");
					// tempssn 이라는 임시 테이블을 생성한다, columns은 fname, lname, ssn, superssn으로 구성되며, engine은 memory를 사용한다
					
					// 아래 코드는 처음 테스트시 데이터를 넣기 위해 사용한 코드
//					boolean success = false;
//					con.setAutoCommit(false);
					//when transaction is executed normally, set success to true
					//if success is true, commit; otherwise rollback
//					try {
//						// transaction starts here
//						stmt.executeUpdate("insert into EMPLOYEE(fname, lname, ssn, superssn) values('UUU', 'UUU', '000000001', '999887777')");
//						stmt.executeUpdate("insert into EMPLOYEE(fname, lname, ssn, superssn) values('VVV', 'VVV', '000000002', '000000001')");
//						stmt.executeUpdate("insert into EMPLOYEE(fname, lname, ssn, superssn) values('WWW', 'WWW', '000000003', '000000002')");
//						stmt.executeUpdate("insert into EMPLOYEE(fname, lname, ssn, superssn) values('XXX', 'XXX', '000000004', '000000003')");
//						stmt.executeUpdate("insert into EMPLOYEE(fname, lname, ssn, superssn) values('YYY', 'YYY', '000000005', '000000004')");
//						stmt.executeUpdate("insert into EMPLOYEE(fname, lname, ssn, superssn) values('ZZZ', 'ZZZ', '000000006', '000000005')");
//						success = true;
//					} catch (Exception e) {
//						System.out.println("Exception occurred. Transaction will be roll backed");
//						System.out.println("SQL State: " + ((SQLException)e).getSQLState());
//						System.out.println("SQL Error Message: " + e.getMessage());
//					} finally {
//						try {
//							if (success) {
//								con.commit();
//								System.out.println("tempssn table create.");
//							}
//							else {
//								con.rollback();
//							}
//						} catch (SQLException sqle) {
//							sqle.printStackTrace();
//						}
//					}
//					
//					con.setAutoCommit(true);
					
					String query = "(select fname, lname, ssn, superssn from EMPLOYEE where superssn = ?)"; // 입력 받은 ssn을 superssn으로 하는 tuple을 찾는다.
					PreparedStatement pstmt = con.prepareStatement(query); // preparedstatement를 통해 query를 먼저 보내둔다.
					String superssn = readEntry("Enter a ssn: "); // 찾을 ssn을 입력 받음
					
					pstmt.clearParameters(); // 이미 parameter가 있다면 제거
					pstmt.setString(1, superssn); // 입력받은 superssn을 prepared에 보낸다.
					ResultSet rset = pstmt.executeQuery(); // prepared query의 결과 set을 받는다.
					
					int n = 1; // level을 표현하기 위한 숫자 인자
					
					// Process the ResultSet
					while(rset.next()) { // 찾는 결과 row를 탐색한다.
						String r_fname = rset.getString(1); // fname 값을 가져온다
						String r_lname = rset.getString(2); // lname 값을 가져온다
						String r_ssn = rset.getString(3); // ssn 값을 가져온다
						String r_superssn = rset.getString(4); // superssn 값을 가져온다
						
						System.out.println(r_ssn + " at level " + n); // 처음 입력 받은 ssn을 superssn으로 하는 employee를 출력, level = 1
						
						String query_test = String.format("insert into tempssn(fname, lname, ssn, superssn) values ('%s', '%s', '%s', '%s')", r_fname, r_lname, r_ssn, r_superssn);				
						stmt.executeUpdate(query_test); // tempssn table에 해당 tuples의 결과를 저장해둔다.
					}
					
					boolean t = true; // loop를 종료하기 위한 변수
					
					while(t) { // t가 true일 때 loop를 돈다.
						
						n = n+1; // 루프가 반복될 때마다 level이 1씩 상승한다.
						String query2 = "select e.fname, e.lname, e.ssn, e.superssn from EMPLOYEE e JOIN tempssn sv ON e.superssn = sv.ssn";
						// join 문을 통해 tempssn에 있는 ssn과 employee에 superssn이 같은 employee tuple들을 가져온다.
						
						ResultSet rset2 = stmt.executeQuery(query2); // query 결과를 받아온다.
						if(!rset2.isBeforeFirst()) { // 만약 query의 결과가 빈 resultset일 경우
							t = false; // while loop를 종료한다.
						}
						while(rset2.next()) { // query의 결과가 빈 resultset이 아닌 경우
							
							String ssn = rset2.getString(3); // 조인 조건을 만족하는  employee ssn값을 가져옴
							System.out.println(ssn + " at level " + n); // level = n
							
							String r_fname = rset2.getString(1); // fname 값을 가져온다
							String r_lname = rset2.getString(2); // lname 값을 가져온다
							String r_ssn = rset2.getString(3); // ssn 값을 가져온다
							String r_superssn = rset2.getString(4); // superssn 값을 가져온다
							
							Statement stmt2 = con.createStatement(); // 새 query를 보낼 statement를 생성한다.
							String query_test = String.format("delete from tempssn"); // tempssn에 있는 값들을 제거한다.
							stmt2.executeUpdate(query_test); // 위의 query를 실행
							
							String query_test2 = String.format("insert into tempssn(fname, lname, ssn, superssn) values ('%s', '%s', '%s', '%s')", r_fname, r_lname, r_ssn, r_superssn);				
							stmt2.executeUpdate(query_test2); // level n 에 해당하는 tuple들을 tempssn에 추가한다.
						}
						rset2.close(); // 이전 결과는 더 이상 필요없으므로 제거
					}
					System.out.println("END OF LIST");
					
					//Close objects
					rset.close();
					pstmt.close();
					con.close();
					
				}
				catch (SQLException ex) {
					System.out.println("SQLException" + ex);
				}
				catch (Exception ex) {
					System.out.println("Exception:" + ex);
				}
				
			}
			
			private static String getPassword() {
				final String password, message = "Enter password";
				if(System.console() == null)
				{
					final JPasswordField pf = new JPasswordField();
					password = JOptionPane.showConfirmDialog(null, pf, message,
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE ) == JOptionPane.OK_OPTION ?
									new String(pf.getPassword()) : "";
				}
				else
					password = new String(System.console().readPassword("%s> ", message));
				
				return password;
			}
			
			private static String readEntry(String prompt) {
				try {
					StringBuffer buffer = new StringBuffer();
					System.out.print(prompt);
					System.out.flush();
					int c = System.in.read();
					while (c != '\n' && c != -1) {
						buffer.append((char)c);
						c = System.in.read();
					}
					return buffer.toString().trim();
				} catch (IOException e) {
					return "";
				}
			}


}
