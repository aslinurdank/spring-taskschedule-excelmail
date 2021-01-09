package com.suonic.service;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class ScheduleDbService extends NamedParameterJdbcDaoSupport {
	private static Logger LOGGER = LoggerFactory
			.getLogger(ScheduleDbService.class);

	
	public List<Map<String, Object>> getIO(Object userid) {
		try {
			String sql = " SELECT USERID,FULL_NAME, AVERAGE_WRKTIME<>0 THEN TO_CHAR(TRUNC (AVERAGE_WRKTIME  / 60))  " +
					"             || ' hour '  " +
					"             || TO_CHAR(TRUNC (MOD (AVERAGE_WRKTIME , 60)))  " +
					"             || ' minute'  " +
					"             AVERAGE_WRKTIME FROM POOL   " 
				
 ;

			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			List<Map<String, Object>> ldisList = getNamedParameterJdbcTemplate().queryForList(sql,
					namedParameters);
			return ldisList.size() > 0 ? ldisList : null;
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("ScheduleDbService getIO error:", e);
			return null;
		}
	}
	

}