package ch.unibas.cs.dbis.cineast.core.features;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.color.RGBContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorUtils;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class SimplePerceptualHash extends AbstractFeatureModule {

	public SimplePerceptualHash() {
		super("features.SimplePerceptualHash", 1);
	}

	@Override
	public void processShot(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
			persist(shot.getId(), hash(shot.getMostRepresentativeFrame().getImage()));
		}
	}

//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		ResultSet rset = this.selector.select("SELECT 0 as dist, shotId FROM features.SimplePerceptualHash, q WHERE SimplePerceptualHash.hash = " + hash(qc.getMostRepresentativeFrame().getImage()) + " LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(long shotId) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select("WITH q AS (SELECT hash FROM features.SimplePerceptualHash WHERE shotid = " + shotId + ") SELECT 0 as dist, shotId FROM features.SimplePerceptualHash, q WHERE SimplePerceptualHash.hash = q.hash LIMIT " + limit);
//		return manageResultSet(rset);
//	}
	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT 0 as dist, shotId FROM features.SimplePerceptualHash, q, c WHERE shotid = c.filter AND SimplePerceptualHash.hash = " + hash(qc.getMostRepresentativeFrame().getImage()) + " LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//	
//	@Override
//	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + ", q AS (SELECT hash FROM features.SimplePerceptualHash WHERE shotid = " + shotId + ") SELECT 0 as dist, shotId FROM features.SimplePerceptualHash, q, c WHERE shotid = c.filter AND SimplePerceptualHash.hash = q.hash LIMIT " + limit);
//		return manageResultSet(rset);
//	}

	private void persist(String shotId, long hash) {
		PersistentTuple tuple = phandler.generateTuple(shotId, hash);
		this.phandler.persist(tuple);
	}
	
	public static long hash(MultiImage img){
		ArrayList<ReadableRGBContainer> rgbs = new ArrayList<>();
		int[] colors = img.getColors();
		for(int c : colors){
			rgbs.add(new ReadableRGBContainer(c));
		}
		float avg = new ReadableRGBContainer(ColorUtils.getAvg(colors)).getLuminance();
		
		ArrayList<LinkedList<ReadableRGBContainer>> partitions = GridPartitioner.partition(rgbs, img.getWidth(), img.getHeight(), 8, 8);
		
		long hash = 0l;
		
		RGBContainer container = new RGBContainer(0);
		
		for(int i = 0; i < 64; ++i){
			ColorUtils.getAvg(partitions.get(i), container);
			float lum = container.getLuminance();
			
			if(lum > avg){
				hash |= (1l << i);
			}
		}
		return hash;
	}

	@Override
	public void init(PersistencyWriter<?> phandler) {
		super.init(phandler);
		this.phandler.setFieldNames("id", "hash");
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
