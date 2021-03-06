/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.genericjpa.test.batchindexing;

/**
 * @author Martin Braun
 */
public class MassIndexerTest {

	/*
	private static final int COUNT = 2153;
	private static final int SORCERER_COUNT_EACH = 3;
	private EntityManagerFactory emf;
	private EntityManager em;
	private JPASearchFactoryAdapter searchFactory;

	//this one is only for Place entities
	private MassIndexer massIndexer;

	@Test
	public void test() throws InterruptedException {
		System.out.println( "starting MassIndexer testCustomUpdatedEntity!" );

		FullTextEntityManager fem = this.searchFactory.getFullTextEntityManager( this.em );
		fem.beginSearchTransaction();
		fem.purgeAll( Sorcerer.class );
		fem.commitSearchTransaction();

		//make sure every Sorcerer is deleted
		assertEquals( 0, fem.createFullTextQuery( new MatchAllDocsQuery(), Sorcerer.class ).getResultSize() );

		this.massIndexer.threadsToLoadObjects( 15 );
		this.massIndexer.batchSizeToLoadObjects( 100 );
		this.massIndexer.batchSizeToLoadIds( 500 );
		long pre = System.currentTimeMillis();
		try {
			this.massIndexer.startAndWait();
		}
		catch (InterruptedException e) {
			throw new SearchException( e );
		}
		long after = System.currentTimeMillis();
		System.out.println( "indexed " + COUNT + " root entities (3 sub each) in " + (after - pre) + "ms." );

		//make sure no Sorcerer was added
		assertEquals( 0, fem.createFullTextQuery( new MatchAllDocsQuery(), Sorcerer.class ).getResultSize() );

		assertEquals( COUNT, fem.createFullTextQuery( new MatchAllDocsQuery(), Place.class ).getResultSize() );
		System.out.println( "finished MassIndexer testCustomUpdatedEntity!" );
	}

	@Test
	public void provokeException() throws InterruptedException {
		System.out.println( "starting provokeException" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "************ Exceptions expected! ************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		final EntityProvider entityProvider = new EntityProvider() {
			@Override
			public Object get(Class<?> entityClass, Object id, Map<String, Object> hints) {
				throw new RuntimeException( "Exceptions are expected in this testCustomUpdatedEntity!" );
			}

			@Override
			public List getBatch(Class<?> entityClass, List<Object> id, Map<String, Object> hints) {
				throw new RuntimeException( "Exceptions are expected in this testCustomUpdatedEntity!" );
			}

			@Override
			public void close() throws IOException {

			}
		};
		this.massIndexer.entityProvider( entityProvider ).startAndWait();
		Thread.sleep( 1000 );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "************ Exceptions expected! ************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "finished provokeException" );
	}

	@Test
	public void testManualEntityProvider() throws InterruptedException {
		System.out.println( "starting testManualEntityProvider" );
		boolean[] usedManual = new boolean[1];
		final EntityProvider entityProvider = new EntityProvider() {
			@Override
			public Object get(Class<?> entityClass, Object id, Map<String, Object> hints) {
				usedManual[0] = true;
				return null;
			}

			@Override
			public List getBatch(Class<?> entityClass, List<Object> id, Map<String, Object> hints) {
				usedManual[0] = true;
				return Collections.emptyList();
			}

			@Override
			public void close() throws IOException {

			}
		};
		this.massIndexer.entityProvider( entityProvider ).startAndWait();
		assertTrue( usedManual[0] );
		System.out.println( "finished testManualEntityProvider" );
	}

	@Test
	public void testCancel() {
		System.out.println( "starting testCancel" );
		Future<?> future = this.massIndexer.start();
		future.cancel( true );
		System.out.println( "finished testCancel" );
	}

	@Test
	public void testFromSearchFactory() {
		System.out.println( "starting testFromSearchFactory" );
		try {
			//well, testing for all Entity types is kinda convenient here
			this.searchFactory.createMassIndexer()
					.threadsToLoadObjects( 15 )
					.batchSizeToLoadObjects( 100 )
					.progressMonitor( this.progress() )
					.startAndWait();

			FullTextEntityManager fem = this.searchFactory.getFullTextEntityManager( this.em );
			assertEquals( COUNT, fem.createFullTextQuery( new MatchAllDocsQuery(), Place.class ).getResultSize() );
			assertEquals(
					COUNT * SORCERER_COUNT_EACH, fem.createFullTextQuery(
							new MatchAllDocsQuery(),
							Sorcerer.class
					).getResultSize()
			);
		}
		catch (InterruptedException e) {
			throw new SearchException( e );
		}
		System.out.println( "finished testFromSearchFactory" );
	}

	@Before
	public void setup() {
		this.emf = Persistence.createEntityManagerFactory( "EclipseLink_HSQLDB" );
		Properties properties = new Properties();
		properties.setProperty( Constants.SEARCH_FACTORY_NAME_KEY, "testCustomUpdatedEntity" );
		properties.setProperty( Constants.TRIGGER_SOURCE_KEY, HSQLDBTriggerSQLStringSource.class.getName() );
		properties.setProperty( Constants.SEARCH_FACTORY_TYPE_KEY, "sql" );
		properties.setProperty(
				Constants.TRIGGER_CREATION_STRATEGY_KEY,
				Constants.TRIGGER_CREATION_STRATEGY_DROP_CREATE
		);
		properties.setProperty( "hibernate.search.default.directory_provider", "filesystem" );
		properties.setProperty( "hibernate.search.default.indexBase", "target/indexes" );
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

			int sorcCount = 0;
			for ( int i = 0; i < COUNT; ++i ) {
				Place place = new Place();
				place.setName( "Place" + i );

				Set<Sorcerer> sorcs = new HashSet<>();
				for ( int j = 0; j < SORCERER_COUNT_EACH; ++j ) {
					Sorcerer sorc = new Sorcerer();
					sorc.setName( "Sorcerer" + sorcCount++ );
					sorcs.add( sorc );
					sorc.setPlace( place );
					em.persist( sorc );
				}
				place.setSorcerers( sorcs );
				em.merge( place );
			}

			tx.commit();
		}
		finally {
			if ( em != null ) {
				em.close();
			}
		}
		this.em = this.emf.createEntityManager();
		this.massIndexer = new MassIndexerImpl(
				this.emf,
				this.searchFactory.getSearchIntegrator(),
				Arrays.asList( Place.class ),
				null
		);
		this.massIndexer.progressMonitor( this.progress() );
	}

	private MassIndexerProgressMonitor progress() {
		return new MassIndexerProgressMonitor() {

			@Override
			public void objectsLoaded(Class<?> entityType, int count) {
				System.out.println( entityType + " objects loaded: " + count );
			}

			@Override
			public void documentsBuilt(Class<?> entityType, int count) {
				System.out.println( entityType + " documents built: " + count );
			}

			@Override
			public void idsLoaded(Class<?> entityType, int count) {
				System.out.println( entityType + " loaded ids: " + count );
			}

			@Override
			public void documentsAdded(int count) {
				System.out.println( "documents added: " + count );
			}

		};
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
