package ch.unibas.cs.dbis.cineast.core.features;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StatElement;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.descriptor.EdgeImg;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class EdgeGrid16 extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public EdgeGrid16(){
		super("features.EdgeGrid16", 124f / 4f);
	}

	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			persist(shot.getId(), getEdges(shot.getMostRepresentativeFrame().getImage()));
		}
		LOGGER.exit();
	}

	private static FloatVector getEdges(MultiImage img){
		StatElement[] stats = new StatElement[256];
		for(int i = 0; i < 256; ++i){
			stats[i] = new StatElement();
		}
		List<Boolean> edgePixels = EdgeImg.getEdgePixels(img, new ArrayList<Boolean>(img.getWidth() * img.getHeight()));
		ArrayList<LinkedList<Boolean>> partition = GridPartitioner.partition(edgePixels, img.getWidth(), img.getHeight(), 16, 16);
		for(int i = 0; i < partition.size(); ++i){
			LinkedList<Boolean> edge = partition.get(i);
			StatElement stat = stats[i];
			for(boolean b : edge){
				stat.add(b ? 1 : 0);
			}
		}
		float[] f = new float[256];
		for(int i = 0; i < 256; ++i){
			f[i] = stats[i].getAvg();
		}
		
		return new FloatVectorImpl(f);
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		FloatVector query = getEdges(sc.getMostRepresentativeFrame().getImage());
		return getSimilar(query.toArray(null), qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
