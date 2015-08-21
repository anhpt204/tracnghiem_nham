/* *********************************************************************** *
 * project: org.matsim.contrib.networkEditor
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 Daniel Ampuero
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.networkEditor.run;

import javax.swing.JFileChooser;

import org.matsim.contrib.networkEditor.utils.OsmImport;
import org.matsim.contrib.networkEditor.visualizing.NetVisualizerPanel;

/**
 * @author danielmaxx
 */
public class NetworkEditor extends javax.swing.JFrame {

	public NetVisualizerPanel netVisFrame;

	/** Initializes and starts the NetworkEditor */
	public NetworkEditor() {
		initComponents();
		initVis();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        btnSaveNet = new javax.swing.JButton();
        btnSaveCounts = new javax.swing.JButton();
        btnReadNet = new javax.swing.JButton();
        btnReadCounts = new javax.swing.JButton();
        btnReadOsm = new javax.swing.JButton();
        btnExportShp = new javax.swing.JButton();
        btnCleanNet = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
						public void componentResized(java.awt.event.ComponentEvent evt) {
                resizeHandler(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 977, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 594, Short.MAX_VALUE)
        );

        btnSaveNet.setText("Save Network");
        btnSaveNet.setEnabled(false);
        btnSaveNet.addActionListener(new java.awt.event.ActionListener() {
            @Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveNetActionPerformed(evt);
            }
        });

        btnSaveCounts.setText("Save Counts");
        btnSaveCounts.setEnabled(false);
        btnSaveCounts.addActionListener(new java.awt.event.ActionListener() {
            @Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveCountsActionPerformed(evt);
            }
        });

        btnReadNet.setText("Read Network");
        btnReadNet.addActionListener(new java.awt.event.ActionListener() {
            @Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadNetActionPerformed(evt);
            }
        });

        btnReadCounts.setText("Read Counts");
        btnReadCounts.setEnabled(false);
        btnReadCounts.addActionListener(new java.awt.event.ActionListener() {
            @Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadCountsActionPerformed(evt);
            }
        });

        btnReadOsm.setText("Read OSM");
        btnReadOsm.addActionListener(new java.awt.event.ActionListener() {
            @Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadOsmActionPerformed(evt);
            }
        });

        btnExportShp.setText("Export as .shp");
        btnExportShp.addActionListener(new java.awt.event.ActionListener() {
            @Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportShpActionPerformed(evt);
            }
        });

        btnCleanNet.setText("Clean Network");
        btnCleanNet.setEnabled(false);
        btnCleanNet.addActionListener(new java.awt.event.ActionListener() {
            @Override
						public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCleanNetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(92, Short.MAX_VALUE)
                .addComponent(btnReadOsm)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReadNet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReadCounts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSaveNet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSaveCounts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCleanNet, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExportShp)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveCounts)
                    .addComponent(btnSaveNet)
                    .addComponent(btnReadCounts)
                    .addComponent(btnExportShp)
                    .addComponent(btnCleanNet)
                    .addComponent(btnReadNet)
                    .addComponent(btnReadOsm))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void resizeHandler(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_resizeHandler
		/*board.setSize(this.getWidth(), this.getHeight()-30);
        board.repaint();*/
	}//GEN-LAST:event_resizeHandler

	private void btnSaveNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveNetActionPerformed
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("Save");
		int state = chooser.showSaveDialog(null);
		String path = "";
		if(state == JFileChooser.APPROVE_OPTION) {
			path = chooser.getSelectedFile().getAbsolutePath();
			this.netVisFrame.saveNetwork(path);
		}
	}//GEN-LAST:event_btnSaveNetActionPerformed

	private void btnSaveCountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCountsActionPerformed
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("Save");
		int state = chooser.showSaveDialog(null);
		if(state == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getAbsolutePath();
			netVisFrame.saveCounts(path);
		}
	}//GEN-LAST:event_btnSaveCountsActionPerformed

	private void btnReadNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadNetActionPerformed
		JFileChooser chooser = new JFileChooser();
		int state = chooser.showOpenDialog(null);
		if(state == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getAbsolutePath();
			if(!netVisFrame.loadNetFromFile(path))
				return;
			btnSaveNet.setEnabled(true);
			btnSaveCounts.setEnabled(true);
			btnReadCounts.setEnabled(true);
			btnCleanNet.setEnabled(true);
			repaint();
		}
	}//GEN-LAST:event_btnReadNetActionPerformed

	private void btnReadCountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadCountsActionPerformed
		JFileChooser chooser = new JFileChooser();
		int state = chooser.showOpenDialog(null);
		String path = "";
		if(state == JFileChooser.APPROVE_OPTION) {
			path = chooser.getSelectedFile().getAbsolutePath();
			netVisFrame.loadCountsFromFile(path);
		}
	}//GEN-LAST:event_btnReadCountsActionPerformed

	private void btnReadOsmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadOsmActionPerformed
		JFileChooser chooser = new JFileChooser();
		int state = chooser.showOpenDialog(null);
		String path = "";
		if (state == JFileChooser.APPROVE_OPTION) {
			OsmImport dlg = new OsmImport(this);
			dlg.setVisible(true);
			String crs = dlg.getIdentifiedCrsString();
			if (crs == null) {
				return;
			}
			path = chooser.getSelectedFile().getAbsolutePath();
			if(!netVisFrame.loadNetFromOSM(path, crs)) {
				return;
			}
			btnSaveNet.setEnabled(true);
			btnSaveCounts.setEnabled(true);
			btnReadCounts.setEnabled(true);
			btnCleanNet.setEnabled(true);
			repaint();
		}
	}//GEN-LAST:event_btnReadOsmActionPerformed

	private void btnExportShpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportShpActionPerformed
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("Save");
		int state = chooser.showSaveDialog(null);
		if(state == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getAbsolutePath();
			this.netVisFrame.saveNetworkAsESRI(path);
		}
	}//GEN-LAST:event_btnExportShpActionPerformed

	private void btnCleanNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCleanNetActionPerformed
		netVisFrame.cleanNetwork();
	}//GEN-LAST:event_btnCleanNetActionPerformed

	private void initVis() {
		netVisFrame = new NetVisualizerPanel();
		//netVisFrame.setSize(this.getSize().width-20, this.getSize().height-30);
		this.mainPanel.add(netVisFrame);
		//netVisFrame.setVisible(true);
		this.setSize(this.getWidth() + 1, this.getHeight()); // force relayout
		this.mainPanel.revalidate();
		this.validate();
		this.invalidate();
	}


	public static void main(String args[]){
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				NetworkEditor vis = new NetworkEditor();
				vis.setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCleanNet;
    private javax.swing.JButton btnExportShp;
    private javax.swing.JButton btnReadCounts;
    private javax.swing.JButton btnReadNet;
    private javax.swing.JButton btnReadOsm;
    private javax.swing.JButton btnSaveCounts;
    private javax.swing.JButton btnSaveNet;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

}
