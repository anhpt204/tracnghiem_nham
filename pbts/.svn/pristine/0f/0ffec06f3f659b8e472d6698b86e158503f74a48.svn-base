package ktuan;

import java.util.ArrayList;
import java.util.TreeSet;

public class GraphSARP {

	public MapSARP _map;
	public int _numberNode;
	public ArrayList<EdgeMap>[] _neighbor;
	public double[] _d;
	public int[] _preNode;
	public double[] _preDistance;
	public int[] _timeMin;
	public int[] _timeMax;

	public GraphSARP(MapSARP map) {
		this._map = map;
		createGraphFromMap();
	}

	private void createGraphFromMap() {
		System.out.println("Start: Create Graph From Map");
		ArrayList<EdgeMap> edges = _map._listEdgeMap;
		_numberNode = 0;
		for (int i = 0; i < edges.size(); i++) {
			if (edges.get(i)._id1 > _numberNode)
				_numberNode = edges.get(i)._id1;
			if (edges.get(i)._id2 > _numberNode)
				_numberNode = edges.get(i)._id2;
		}
		_neighbor = new ArrayList[_numberNode + 2];
		for (int i = 0; i <= _numberNode; i++)
			_neighbor[i] = new ArrayList<EdgeMap>();
		for (int cs = 0; cs < edges.size(); cs++) {
			_neighbor[edges.get(cs)._id1]
					.add(new EdgeMap(edges.get(cs), false));
		}
		_d = new double[_numberNode + 1];
		_preNode = new int[_numberNode + 1];
		_preDistance = new double[_numberNode + 1];
		_timeMin = new int[_numberNode + 1];
		_timeMax = new int[_numberNode + 1];
		System.out.println("Finish: Create Graph From Map");
	}

	final class Pair implements Comparable<Pair> {
		int node;
		double distance;

		public Pair(int _node, double _distance) {
			node = _node;
			distance = _distance;
		}

		public int compareTo(Pair Ob) {
			if (Math.abs(distance - Ob.distance) < 0.00000001) {
				if (node < Ob.node)
					return -1;
				if (node > Ob.node)
					return 1;
				return 0;
			}
			if (distance < Ob.distance)
				return -1;
			return 1;
		}

		public String toString() {
			return String.format("Pair[%6d|%14.8f]", node, distance);
		}
	}

	private TreeSet<Pair> _set;

	public void dijkstraHeap(int rootNode) {
		System.out.println("Start: Dijkstra Heap " + rootNode);
		initDijkstraHeap(rootNode);
		while (!_set.isEmpty()) {
			Pair p = _set.first();
			_set.remove(_set.first());
			int u = p.node;
			// System.out.println("["+p.node+"] = "+p.distance);
			if (Math.abs(p.distance - _d[u]) > ParameterSARP.EPSILON)
				continue;
			for (int cs = 0; cs < _neighbor[u].size(); cs++) {
				EdgeMap e = _neighbor[u].get(cs);
				int v = e._id2;
				double w = e._distance;
				int tmin = e._minTime;
				int tmax = e._maxTime;

				if (_d[v] > _d[u] + w) {
					_d[v] = _d[u] + w;
					_preNode[v] = u;
					_preDistance[v] = w;
					_set.add(new Pair(v, _d[v]));
					_timeMin[v] = _timeMin[u] + tmin;
					_timeMax[v] = _timeMax[u] + tmax;
				}
			}
		}
		System.out.println("Finish: Dijkstra Heap " + rootNode);

	}

	private void initDijkstraHeap(int rootNode) {
		_set = new TreeSet<Pair>();
		for (int cs = 1; cs <= _numberNode; cs++) {
			_d[cs] = (double)Integer.MAX_VALUE / 3;
			_preNode[cs] = -1;
			_preDistance[cs] = 0.0;
			_timeMin[cs]=_timeMax[cs]=0;
		}
		_d[rootNode] = 0.0;
		_set.add(new Pair(rootNode, _d[rootNode]));
	}

}
