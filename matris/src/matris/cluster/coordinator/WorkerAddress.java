package matris.cluster.coordinator;

public class WorkerAddress {

	private String host;

	private int port;

	public WorkerAddress(String host, int port) {

		this.host = host;
		this.port = port;
	}

	public String getHost() {

		return host;
	}

	public int getPort() {

		return port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkerAddress other = (WorkerAddress) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public String toString() {

		return host + ":" + port;
	}
}
