package database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TimelineTest {
	static private DBM sut;

	static Timeline[] timelines = new Timeline[4];
	static Timeline[] timeline = new Timeline[4];

	@BeforeAll
	static void init() throws Exception {
		sut = new DBM("jdbc:mysql://localhost?useTimezone=true&serverTimezone=UTC", "root", "AJnuHA^8VKHht=uB",
				"project");
		DBM.setupSchema();
		DBM.createTestData();
	}

	static void createTestDB() throws SQLException {
		Timeline TL1 = new Timeline();
		TL1.setName("Normal Name");
		TL1.setDescription("Normal Timeline Description with normal stuff in it.");
		TL1.setID(1);
		TL1.setOwnerID(1);
		TL1.setScale(1);
		Timeline TL2 = new Timeline();
		TL2.setName("");
		TL2.setDescription("d");
		TL2.setID(2);
		TL2.setScale(3);
		Timeline TL3 = new Timeline();
		TL3.setName("SW chars in name ÄÖÅ");
		TL3.setDescription("SW chars in Description ÄÖÅ");
		TL3.setID(3);
		Timeline TL4 = new Timeline();
		TL4.setName("Supper long name and description test");
		TL4.setDescription(
				"Lorem ipsum dolor sit amet, simul senserit consulatu ut his, autem legimus eum at. In labitur reformidans per. Vis quidam facilisi accusamus no, dolor verterem quaestio sed te, erant civibus intellegam vis cu. Vix ex pertinacia definitionem, et nam purto dignissim reformidans. Nec eu veniam maiestatis dissentiet, fierent nominavi probatus ex quo. Mel in error tamquam ceteros.\r\n"
						+ "Affert quidam copiosae an pro, vel ne quas nullam inermis. Velit laudem eos ut, labitur necessitatibus duo ex. Etiam viderer volumus his ex, perpetua similique inciderint est et. Mea te quidam copiosae scribentur, munere torquatos definitionem et per. Per id nobis nostro. Te maiorum mandamus interpretaris sea, ne vis discere voluptatibus. Et nec nibh appellantur, est tractatos dignissim moderatius ad.\r\n"
						+ "Sed ne inani mucius sensibus. Eos nullam melius et, et epicurei delectus usu, an has ferri semper definiebas. Voluptaria conclusionemque nam ad, ne est cibo oportere. Apeirian eleifend duo ne, putent dolores sadipscing has ad.\r\n"
						+ "In vel ubique epicuri temporibus. Pri assum iudico adipiscing an, odio interesset definitionem et eam, an mea suas prima essent. Ad eos nusquam invenire, tamquam deseruisse ei est. Ne per assum tibique mentitum, eos amet aperiri phaedrum ne. Te usu vidit ignota antiopam, mundi ullamcorper philosophia ne eum, aperiam legimus civibus ut his.\r\n"
						+ "\r\n"
						+ "Qui ei sale copiosae, fugit quodsi scaevola cu mel. Vix no quem aeterno labitur. Vis vero aliquam incorrupte te. Sit nullam luptatum forensibus ne, ne nec facilis posidonium. Ne vix perpetua mediocritatem, ea mea utinam mnesarchum, debet viderer recusabo no eos. Detracto conceptam te eum, ad duo sale dolorem, ignota consequat suscipiantur ei vel. Eu est noster timeam.");
		TL4.setID(4);
		TL4.setScale(4);
		timelines[0] = TL1;
		timelines[1] = TL2;
		timelines[2] = TL3;
		timelines[3] = TL4;

	}

	@AfterAll
	static void tearDown() throws SQLException {
		DBM.conn.createStatement().execute("DROP DATABASE IF EXISTS test");
		DBM.conn.close();
	}

	@Test
	void getInsertQueryTest() throws SQLException {
		// method turns default values to "** NOT SPECIFIED **" so this fails, full fake
		// data in test class
		String sql = "INSERT INTO `timelines` ( `Scale`,`TimelineName`, `TimelineDescription`, `Theme`,`StartYear`,`StartMonth`,`StartDay`,`StartHour`"
				+ ",`StartMinute`,`StartSecond`,`StartMillisecond`,`EndYear`,`EndMonth`,`EndDay`,`EndHour`,`EndMinute`,`EndSecond`,"
				+ "`EndMillisecond`,`TimelineOwner`,`Keywords`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement out = DBM.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int i = 0; i < timelines.length; i++) {
			assertEquals(out.toString(), timelines[i].getInsertQuery().toString());
		}
		// add exeption testing
	}

	@Test
	void getUpdateQueryTest() throws SQLException {
		String sql = "DELETE FROM `timelines` WHERE (`TimelineID` = ?)";
		PreparedStatement out = DBM.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int i = 0; i < timelines.length; i++) {
			assertEquals(out.toString(), timelines[i].getDeleteQuery().toString());
		}

	}

	@Test
	void getDeleteQueryTest() throws SQLException {
		String sql = "DELETE FROM `timelines` WHERE (`TimelineID` = ?)";
		PreparedStatement out = DBM.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int i = 0; i < timelines.length; i++) {
			assertEquals(out.toString(), timelines[i].getDeleteQuery().toString());
		}
	}

	@Test
	void createFromDBTest() throws SQLException {
		// Create objects from the DB and see if they are 4(cause I inserted 4)
		ResultSet rs;
		PreparedStatement stmt = DBM.conn.prepareStatement("SELECT COUNT(*) FROM timelines");
		rs = stmt.executeQuery();
		rs.next();
		int actual = rs.getInt(1);
		assertEquals(timelines.length, actual);

		// See if the database objects are the same as the ones I pushed
		PreparedStatement stmt1 = DBM.conn.prepareStatement("SELECT * FROM timelines");
		List<Timeline> TimelineList = DBM.getFromDB(stmt1, new Timeline());
		for (int i = 0; i < timelines.length; i++) {
			assertEquals(timelines[i].getName(), TimelineList.get(i).getName());
			assertEquals(timelines[i].getDescription(), TimelineList.get(i).getDescription());
		}
	}

	@Test
	void toStringTest() {
		// "Name: " + timelineName + " Description: " + timelineDescription
		for (int i = 0; i < timelines.length; i++) {
			String Actual = timelines[i].toString();
			String Expected = "Name: " + timelines[i].getName() + " Description: "
					+ timelines[i].getDescription();
			assertEquals(Actual, Expected);
		}
	}

	@Test
	void setTimelineNameTest() {
		for (int i = 0; i < timelines.length; i++) {
			String oldName = timelines[i].getName();
			timelines[i].setName("A Different name");
			assertFalse(oldName.equals(timelines[i].getName()));
		}
	}

	@Test
	void getTimelineIDTest() {
		for (int i = 0; i < timelines.length; i++) {
			int IDGot = timelines[i].getID();
			assertEquals(IDGot, (i + 1));
		}
	}

	@Test
	void getNameTest() {
		for (int i = 0; i < timelines.length; i++) {
			String NewTestingName = "New Testing Name";
			timelines[i].setName(NewTestingName);
			assertEquals(timelines[i].getName(), NewTestingName);
		}
	}

	@Test
	void getScaleTest() {
		for (int i = 0; i < timelines.length; i++) {
			int actual = timelines[i].getScale();
			assertEquals(actual,i);
		}
	}


}
