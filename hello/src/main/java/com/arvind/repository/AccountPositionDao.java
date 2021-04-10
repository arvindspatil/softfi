package com.arvind.repository;

import java.util.List;

import com.arvind.model.AccountPosition;

public interface AccountPositionDao {
	public void insert(AccountPosition position);
	public void delete(int positionId);
	public void update(AccountPosition position);
	public List<AccountPosition> findPositions();
	public List<AccountPosition> findPositionsByAcctId(int acctId);
}
