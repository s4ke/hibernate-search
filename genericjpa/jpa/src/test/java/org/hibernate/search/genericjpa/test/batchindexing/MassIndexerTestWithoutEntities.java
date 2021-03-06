package org.hibernate.search.genericjpa.test.batchindexing;

public class MassIndexerTestWithoutEntities {

	/*private EntityManagerFactory emf;
	private EntityManager em;
	private JPASearchFactoryAdapter searchFactory;
	private MassIndexer massIndexer;

	@Test
	public void test() throws InterruptedException {
		this.massIndexer.startAndWait();
	}

	@Before
	public void setup() {
		this.emf = Persistence.createEntityManagerFactory( "EclipseLink_HSQLDB" );
		Properties properties = new Properties();
		properties.setProperty( Constants.SEARCH_FACTORY_NAME_KEY, "testCustomUpdatedEntity" );
		properties.setProperty( Constants.TRIGGER_SOURCE_KEY, HSQLDBTriggerSQLStringSource.class.getName() );
		properties.setProperty( Constants.SEARCH_FACTORY_TYPE_KEY, "manual-updates" );
		properties.setProperty( "hibernate.search.default.directory_provider", "filesystem" );
		properties.setProperty( "hibernate.search.default.indexBase", "target/indexes" );
		properties.setProperty(
				Constants.TRIGGER_CREATION_STRATEGY_KEY,
				Constants.TRIGGER_CREATION_STRATEGY_DROP_CREATE
		);
		this.searchFactory = (JPASearchFactoryAdapter) Setup.createSearchFactoryController( emf, properties );
		this.searchFactory.pauseUpdating( true );
		EntityManager em = emf.createEntityManager();
		try {
			EntityTransaction tx = em.getTransaction();
			tx.begin();

			em.createQuery( "DELETE FROM Place" ).executeUpdate();
			em.flush();

			em.createQuery( "DELETE FROM Sorcerer" ).executeUpdate();
			em.flush();
			tx.commit();
		}
		finally {
			if ( em != null ) {
				em.close();
			}
		}
		this.massIndexer = new MassIndexerImpl(
				this.emf,
				this.searchFactory.getSearchIntegrator(),
				Arrays.asList( Place.class ),
				null
		);
	}

	@After
	public void shutdown() {
		// has to be shut down first (update processing!)
		try {
			this.searchFactory.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if ( this.em != null ) {
			try {
				this.em.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		if ( this.emf != null ) {
			try {
				this.emf.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}*/

}
