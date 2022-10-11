
public class Point3D {

	// 3D coordinates of the Cartesian space
	public int x, y, z;

	// Constructor
	public Point3D(int x, int y, int z) {

		this.x = x; // this.x = 
		this.y = y;
		this.z = z;

	}

	public double getDistance(Point3D point) {

		double distX = point.x - this.x;
		double distY = point.y - this.y;
		double distZ = point.z - this.z;

		double dist = Math.sqrt(distX * distX + distY * distY + distZ * distZ);

		return dist;
	}
	
	public void add( Point3D point) {
		
		this.x = point.x + this.x;
		this.y = point.y + this.y;
		this.z = point.z + this.z;
		
	}

}
