package com.wwgroup.common.action;


@SuppressWarnings("serial")
public class BaseAction extends AbstractAction {

	/**
	 * @see com.opensymphony.xwork2.ActionSupport#execute()
	 */
	@Override
	public String execute() throws Exception {
		return this.operate();
	}

	/**
	 * @return
	 */
	protected String operate() {
		return NONE;
	}

	@Override
	public Object getModel() {
		// TODO Auto-generated method stub
		return null;
	}

}
