package yl.demo.pathHelper.pathSearching.algrorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.SetCookie;

import android.R.bool;
import android.util.Log;


import yl.demo.pathHelper.db.DBFacade;
import yl.demo.pathHelper.db.DBManager;
import yl.demo.pathHelper.db.model.Corner;
import yl.demo.pathHelper.db.model.Floor;
import yl.demo.pathHelper.db.model.Model;
import yl.demo.pathHelper.db.model.Path;
import yl.demo.pathHelper.pathSearching.location.Location;

/**
 * 寻路算法库
 * @author jackqdyulei
 *
 */
public class SearchPathAlgorithm {

	/**
	 * 通过BFS寻路，效率低。第一版本数据库信息侧用运用
	 * @param sourceCorner	初始拐点
	 * @param targetCorner	终止拐点
	 * @param maxStep		最大步数，超过后返回null
	 * @return	拐点List
	 */
	@Deprecated
	public List<Corner> findPath(Corner sourceCorner, Corner targetCorner,
			int maxStep) {
		Queue<Corner> searchQueue = new LinkedList<Corner>();
		searchQueue.add(sourceCorner);

		ArrayList<Corner> arrayList = new ArrayList<Corner>();
		arrayList.add(sourceCorner);

		HashMap<Corner, List<Corner>> pathHashMap = new HashMap<Corner, List<Corner>>();
		pathHashMap.put(sourceCorner, arrayList);

		while (!searchQueue.isEmpty()) {
			Corner currentHead = searchQueue.poll();

			if (currentHead.getId().equals(targetCorner.getId())) {
				pathHashMap.get(currentHead).add(currentHead);
				return pathHashMap.get(currentHead);
			}

			if (pathHashMap.get(currentHead).size() > maxStep)
				return null;

			Set<Model> paths = DBFacade.findByFieldName(currentHead.getId(),
					Path.class, "cornerIdTo");
			paths.addAll(DBFacade.findByFieldName(currentHead.getId(),
					Path.class, "cornerIdFrom"));

			for (Model model : paths) {
				Path path = (Path) model;
				Integer otherCornerId = path.getCornerIdFrom().equals(
						currentHead.getId()) ? path.getCornerIdTo() : path
						.getCornerIdFrom();
				ArrayList<Corner> tempArrayList = new ArrayList<Corner>();
				tempArrayList.addAll(pathHashMap.get(currentHead));
				Corner otherCorner = (Corner) DBFacade.findById(otherCornerId,
						Corner.class);
				tempArrayList.add(otherCorner);
				pathHashMap.put(otherCorner, tempArrayList);

				searchQueue.add(otherCorner);
			}
		}

		return null;
	}

	/**
	 * 通过Location信息，获取最近的拐点
	 * @param location	
	 * @return
	 */
	public List<Corner> findNearCorner(Location location) {
		Floor floor = (Floor) DBFacade.findById(location.floorId, Floor.class);
		Set<Corner> corners = DBFacade.findCornerByFloorId(floor.getId());
		List<Corner> nearCorners = null;
		boolean hasInsert = false;

		for (Corner corner : corners) {
			hasInsert = false;
			if (nearCorners == null) {
				nearCorners = new ArrayList<Corner>();
				nearCorners.add(corner);
			}
			else {
				for ( int i = 0; i < nearCorners.size(); i++ ) {
					if (Math.hypot(corner.getX() - location.x, corner.getY() - location.y) < Math.hypot(nearCorners.get(i).getX() - location.x, nearCorners.get(i).getY() - location.y)) { 
						nearCorners.add(i,corner);
						hasInsert = true;
						break;
					}
				}
				if (!hasInsert) {
					nearCorners.add(corner);
				}
			}
			if ( nearCorners.size() >= 3 ) {
				nearCorners.remove(nearCorners.size()-1);
			}
		}
		return nearCorners;
	}
	
	/**
	 * 进行拐点间距离重算，测试应用，用户不要随意调用
	 */
	@Deprecated
	public void setPathData() {
		Set<Model> paths = DBFacade.findByFieldName(2, Path.class, "floorId");
		Log.e("Data","size:"+paths.size());
		for (Model model : paths) {
			Path path = (Path) model;
			Corner corner1 = (Corner)DBFacade.findById(path.getCornerIdFrom(), Corner.class);
			Corner corner2 = (Corner)DBFacade.findById(path.getCornerIdTo(), Corner.class);
			double x1 = corner1.getX().doubleValue();
			double y1 = corner1.getY().doubleValue();
			double x2 = corner2.getX().doubleValue();
			double y2 = corner2.getY().doubleValue();
			double length = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
			Log.e("Data", length+"");
			path.setLength(new Double(length));
			
			DBFacade.update(path);
		}
	}
	
	/**
	 * 通过SPFA算法，进行寻路
	 * @param sourceCorner	初始拐点
	 * @param targetCorner	终止拐点
	 * @return 寻路产生的拐点List
	 */
	public List<Corner> findPathBySPFA(Location sourceLocation, Location targetLocation){
		int INF = 60000;
		List<Integer> q;  	//SPFA运算队列
		double[][] graphs;	//图结构
		boolean[] visited;	//记录节点是否已访问
		double distance[];	//SPFA距离数组
		//拐点<->图坐标映射map
		HashMap<Integer, Integer> cornerToGraphsHashMap = new HashMap<Integer, Integer>(); 
		HashMap<Integer, Integer> GraphsToCornerHashMap = new HashMap<Integer, Integer>();
		//路途记录map
	    HashMap<Integer, List<Corner>> pathHashMap = new HashMap<Integer, List<Corner>>();
	    int i,j,N;
	    int id = 0;
	    
	    //获取相近的拐点
	    List<Corner> sourceNearCorners = findNearCorner(sourceLocation);
	    List<Corner> targetNearCorners = findNearCorner(targetLocation);
	    
	    Corner sourceCorner = new Corner(60000,sourceLocation.floorId, Double.valueOf(sourceLocation.x), Double.valueOf(sourceLocation.y));
	    Corner targetCorner = new Corner(60001,targetLocation.floorId, Double.valueOf(targetLocation.x), Double.valueOf(targetLocation.y));
	    
	    
	    //获取拐点和路径信息，进行拐点与图坐标映射，构造图结构
	    Set<Model> cornerSets = DBFacade.findByFieldName(2, Corner.class, "floorId");
	    N = cornerSets.size()+3;
	    for (Model model : cornerSets) {
			Corner corner = (Corner)model;
			cornerToGraphsHashMap.put(corner.getId(), id);
			GraphsToCornerHashMap.put(id, corner.getId());
			id++;
		}
	    cornerToGraphsHashMap.put(sourceCorner.getId(), id);
		GraphsToCornerHashMap.put(id, sourceCorner.getId());
		id++;
		cornerToGraphsHashMap.put(targetCorner.getId(), id);
		GraphsToCornerHashMap.put(id, targetCorner.getId());
	    
	  //构建路径图
	    graphs = new double[N][];
	    for ( i = 0; i < N; i++ ) {
	    	graphs[i] = new double[N];
	    	for ( j = 0; j < N; j++ ) {
	    		graphs[i][j] = INF;
	    	}
	    } 
	    Set<Model> paths = DBFacade.findByFieldName(2, Path.class, "floorId");
	    for (Model model : paths) {
			Path path = (Path)model;
			int cornerFromId = path.getCornerIdFrom();
			int cornerToId = path.getCornerIdTo();
			double length = path.getLength();
			graphs[cornerToGraphsHashMap.get(cornerFromId)][cornerToGraphsHashMap.get(cornerToId)] = length;
			graphs[cornerToGraphsHashMap.get(cornerToId)][cornerToGraphsHashMap.get(cornerFromId)] = length;
		}
	    
	    for (Corner corner : sourceNearCorners) {
	    	double x1 = corner.getX().doubleValue();
	    	double y1 = corner.getY().doubleValue();
	    	double x2 = sourceCorner.getX().doubleValue();
	    	double y2 = sourceCorner.getY().doubleValue();
			double length = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
			graphs[cornerToGraphsHashMap.get(corner.getId())][cornerToGraphsHashMap.get(sourceCorner.getId())] = length;
			graphs[cornerToGraphsHashMap.get(sourceCorner.getId())][cornerToGraphsHashMap.get(corner.getId())] = length;
		}
	    
	    for (Corner corner : targetNearCorners) {
	    	double x1 = corner.getX().doubleValue();
	    	double y1 = corner.getY().doubleValue();
	    	double x2 = targetCorner.getX().doubleValue();
	    	double y2 = targetCorner.getY().doubleValue();
			double length = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
			graphs[cornerToGraphsHashMap.get(corner.getId())][cornerToGraphsHashMap.get(targetCorner.getId())] = length;
			graphs[cornerToGraphsHashMap.get(targetCorner.getId())][cornerToGraphsHashMap.get(corner.getId())] = length;
		}
	    
	    
	    //初始化返回的路径List
	    for ( i = 0; i < N; i++ ) {
	    	List<Corner> corners = new ArrayList<Corner>();
	    	corners.add(sourceCorner);
	    	pathHashMap.put(i, corners);
	    }
	    
	    //获取初始，结束点信息
	    int s = cornerToGraphsHashMap.get(sourceCorner.getId().intValue());
	    int e = cornerToGraphsHashMap.get(targetCorner.getId().intValue());
	    
	    //进行SPFA寻路
	    distance = new double[N];
	    q = new ArrayList<Integer>();
	    visited = new boolean[N];
	    for ( i = 0; i < N; i++ ) 
	    	visited[i] = false;
	    for(i=0;i<N;i++)
	    	distance[i]=INF;
	    distance[s]=0;
	    q.add(s);
	    visited[s]=true;
	    while(!q.isEmpty()){
	        int v=q.get(0);
	        q.remove(0);
	        visited[v]=false;
	        for(i=0;i<N;i++)
	        {
	            if(distance[i]>distance[v]+graphs[i][v])
	            {
	                distance[i] = distance[v]+graphs[i][v];
	                pathHashMap.get(i).clear();
	                List<Corner> corners = new ArrayList<Corner>();
	                corners.addAll(pathHashMap.get(v));
	                Corner nextCorner = (Corner)DBFacade.findById(GraphsToCornerHashMap.get(i), Corner.class);
	                if ( nextCorner != null ) 
	                	corners.add(nextCorner);
	                pathHashMap.remove(i);
	                pathHashMap.put(i, corners);
	                if(!visited[i])
	                {
	                	visited[i]=true;
	                    q.add(i);
	                }
	            }
	        }
	    }
	    pathHashMap.get(e).add(targetCorner);
	    return pathHashMap.get(e);
	}
}
