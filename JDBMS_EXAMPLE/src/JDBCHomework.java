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
					stmt.executeUpdate("drop table if exists tempssn"); // �̹� tempssn memory table�� �����Ѵٸ� ����
					stmt.executeUpdate("create temporary table tempssn(fname varchar(15), lname varchar(15), ssn char(9) primary key, superssn char(9)) engine=MEMORY");
					// tempssn �̶�� �ӽ� ���̺��� �����Ѵ�, columns�� fname, lname, ssn, superssn���� �����Ǹ�, engine�� memory�� ����Ѵ�
					
					// �Ʒ� �ڵ�� ó�� �׽�Ʈ�� �����͸� �ֱ� ���� ����� �ڵ�
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
					
					String query = "(select fname, lname, ssn, superssn from EMPLOYEE where superssn = ?)"; // �Է� ���� ssn�� superssn���� �ϴ� tuple�� ã�´�.
					PreparedStatement pstmt = con.prepareStatement(query); // preparedstatement�� ���� query�� ���� �����д�.
					String superssn = readEntry("Enter a ssn: "); // ã�� ssn�� �Է� ����
					
					pstmt.clearParameters(); // �̹� parameter�� �ִٸ� ����
					pstmt.setString(1, superssn); // �Է¹��� superssn�� prepared�� ������.
					ResultSet rset = pstmt.executeQuery(); // prepared query�� ��� set�� �޴´�.
					
					int n = 1; // level�� ǥ���ϱ� ���� ���� ����
					
					// Process the ResultSet
					while(rset.next()) { // ã�� ��� row�� Ž���Ѵ�.
						String r_fname = rset.getString(1); // fname ���� �����´�
						String r_lname = rset.getString(2); // lname ���� �����´�
						String r_ssn = rset.getString(3); // ssn ���� �����´�
						String r_superssn = rset.getString(4); // superssn ���� �����´�
						
						System.out.println(r_ssn + " at level " + n); // ó�� �Է� ���� ssn�� superssn���� �ϴ� employee�� ���, level = 1
						
						String query_test = String.format("insert into tempssn(fname, lname, ssn, superssn) values ('%s', '%s', '%s', '%s')", r_fname, r_lname, r_ssn, r_superssn);				
						stmt.executeUpdate(query_test); // tempssn table�� �ش� tuples�� ����� �����صд�.
					}
					
					boolean t = true; // loop�� �����ϱ� ���� ����
					
					while(t) { // t�� true�� �� loop�� ����.
						
						n = n+1; // ������ �ݺ��� ������ level�� 1�� ����Ѵ�.
						String query2 = "select e.fname, e.lname, e.ssn, e.superssn from EMPLOYEE e JOIN tempssn sv ON e.superssn = sv.ssn";
						// join ���� ���� tempssn�� �ִ� ssn�� employee�� superssn�� ���� employee tuple���� �����´�.
						
						ResultSet rset2 = stmt.executeQuery(query2); // query ����� �޾ƿ´�.
						if(!rset2.isBeforeFirst()) { // ���� query�� ����� �� resultset�� ���
							t = false; // while loop�� �����Ѵ�.
						}
						while(rset2.next()) { // query�� ����� �� resultset�� �ƴ� ���
							
							String ssn = rset2.getString(3); // ���� ������ �����ϴ�  employee ssn���� ������
							System.out.println(ssn + " at level " + n); // level = n
							
							String r_fname = rset2.getString(1); // fname ���� �����´�
							String r_lname = rset2.getString(2); // lname ���� �����´�
							String r_ssn = rset2.getString(3); // ssn ���� �����´�
							String r_superssn = rset2.getString(4); // superssn ���� �����´�
							
							Statement stmt2 = con.createStatement(); // �� query�� ���� statement�� �����Ѵ�.
							String query_test = String.format("delete from tempssn"); // tempssn�� �ִ� ������ �����Ѵ�.
							stmt2.executeUpdate(query_test); // ���� query�� ����
							
							String query_test2 = String.format("insert into tempssn(fname, lname, ssn, superssn) values ('%s', '%s', '%s', '%s')", r_fname, r_lname, r_ssn, r_superssn);				
							stmt2.executeUpdate(query_test2); // level n �� �ش��ϴ� tuple���� tempssn�� �߰��Ѵ�.
						}
						rset2.close(); // ���� ����� �� �̻� �ʿ�����Ƿ� ����
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
