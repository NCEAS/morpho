package edu.ucsb.nceas.morpho.dataone;

import java.util.Comparator;
import java.util.List;

import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v1.ReplicationPolicy;

public class ReplicationPolicyComparator implements Comparator<ReplicationPolicy> {

	@Override
	public int compare(ReplicationPolicy policy1, ReplicationPolicy policy2) {
		int check = 0;
		int nodeIndex = 0;
		// replication allowed
		if (policy1.getReplicationAllowed() != null && policy2.getReplicationAllowed() != null) {
			check = policy1.getReplicationAllowed().compareTo(policy2.getReplicationAllowed());
			if (check != 0) {
				return check;
			}
		}
		// replica count
		if (policy1.getNumberReplicas() != null && policy2.getNumberReplicas() != null) {
			check = policy1.getNumberReplicas().compareTo(policy2.getNumberReplicas());
			if (check != 0) {
				return check;
			}
		}
		// blocked nodes
		if (policy1.getBlockedMemberNodeList() != null && policy2.getBlockedMemberNodeList() != null) {
			check = Integer.valueOf(policy1.getBlockedMemberNodeList().size()).compareTo(Integer.valueOf(policy2.getBlockedMemberNodeList().size()));
			if (check != 0) {
				return check;
			}
			nodeIndex = 0;
			for (NodeReference node: policy1.getBlockedMemberNodeList()) {
				check = node.getValue().compareTo(policy2.getBlockedMemberNode(nodeIndex).getValue());
				if (check != 0) {
					return check;
				}
			}
		}
		// pref nodes
		if (policy1.getPreferredMemberNodeList() != null && policy2.getPreferredMemberNodeList() != null) {
			check = Integer.valueOf(policy1.getPreferredMemberNodeList().size()).compareTo(Integer.valueOf(policy2.getPreferredMemberNodeList().size()));
			if (check != 0) {
				return check;
			}
			nodeIndex = 0;
			for (NodeReference node: policy1.getPreferredMemberNodeList()) {
				check = node.getValue().compareTo(policy2.getPreferredMemberNode(nodeIndex).getValue());
				if (check != 0) {
					return check;
				}
			}
		}
		// made it here, they are the same
		return 0;
	}

	public static boolean policyMatch(ReplicationPolicy replicationPolicy, List<ReplicationPolicy> otherPolicies) {
		// check if all the values match
		Comparator<ReplicationPolicy> comparator = new ReplicationPolicyComparator();
		for (ReplicationPolicy p: otherPolicies) {
			int check = comparator.compare(replicationPolicy, p);
			if (check != 0) {
				return false;
			}
		}
		return true;
	}

}
