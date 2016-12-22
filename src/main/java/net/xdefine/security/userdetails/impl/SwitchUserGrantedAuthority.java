package net.xdefine.security.userdetails.impl;

import net.xdefine.security.userdetails.Authentication;
import net.xdefine.security.userdetails.GrantedAuthority;

public class SwitchUserGrantedAuthority implements GrantedAuthority {

	private String role;
	private Authentication source;

	public SwitchUserGrantedAuthority(String role, Authentication source) {
		super();
		this.role = role;
		this.source = source;
	}

	@Override
	public String getAuthority() {
		return role;
	}

	public Authentication getSource() {
		return source;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}
