/**
 * Copyright (C) 2018 Christoph Hohmann <reboot@gmx.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.reboot.tvbrowser.trakt.ui.search;

import static io.github.reboot.tvbrowser.trakt.utils.ProgramUtils.getTitles;
import static org.apache.commons.collections4.CollectionUtils.addIgnoreNull;
import io.github.reboot.trakt.api.json.SearchResult;
import io.github.reboot.tvbrowser.trakt.client.TraktClientService;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class SearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(SearchPanel.class);

    private final TraktClientService traktClientService;

    private final MessageService messageService;

    private final SearchProgramPanel programPanel;

    private final JComboBox<String> queryComboBox;

    private final JTable search;

    private List<SearchResult.Type> searchTypes = Collections.emptyList();

    private SearchTableModel searchTableModel;

    private List<SearchPanelListener> listeners = new ArrayList<>();

    /**
     * Create the panel.
     *
     * @wbp.parser.constructor
     */
    public SearchPanel(TraktClientService traktClientService, MessageService messageService) {
        this.traktClientService = traktClientService;
        this.messageService = messageService;

        setLayout(new BorderLayout(0, 0));

        JPanel form = new JPanel();
        form.setBorder(new EmptyBorder(0, 5, 0, 5));
        add(form, BorderLayout.NORTH);
        GridBagLayout gbl_form = new GridBagLayout();
        gbl_form.columnWeights = new double[] { 0.0, 0.0 };
        gbl_form.rowWeights = new double[] { 0.0, 0.0 };
        form.setLayout(gbl_form);

        JLabel programLabel = new JLabel("Program");
        GridBagConstraints gbc_programLabel = new GridBagConstraints();
        gbc_programLabel.fill = GridBagConstraints.BOTH;
        gbc_programLabel.insets = new Insets(0, 0, 5, 5);
        gbc_programLabel.gridx = 0;
        gbc_programLabel.gridy = 0;
        form.add(programLabel, gbc_programLabel);

        programPanel = new SearchProgramPanel();
        GridBagConstraints gbc_programPanel = new GridBagConstraints();
        gbc_programPanel.weightx = 1.0;
        gbc_programPanel.fill = GridBagConstraints.BOTH;
        gbc_programPanel.insets = new Insets(0, 0, 5, 0);
        gbc_programPanel.gridx = 1;
        gbc_programPanel.gridy = 0;
        form.add(programPanel, gbc_programPanel);
        programLabel.setLabelFor(programPanel);

        JLabel queryLabel = new JLabel("Search");
        GridBagConstraints gbc_queryLabel = new GridBagConstraints();
        gbc_queryLabel.fill = GridBagConstraints.BOTH;
        gbc_queryLabel.insets = new Insets(0, 0, 0, 5);
        gbc_queryLabel.gridx = 0;
        gbc_queryLabel.gridy = 1;
        form.add(queryLabel, gbc_queryLabel);

        queryComboBox = new JComboBox<String>();
        queryComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });
        queryComboBox.setEditable(true);
        GridBagConstraints gbc_queryComboBox = new GridBagConstraints();
        gbc_queryComboBox.weightx = 1.0;
        gbc_queryComboBox.fill = GridBagConstraints.BOTH;
        gbc_queryComboBox.gridx = 1;
        gbc_queryComboBox.gridy = 1;
        form.add(queryComboBox, gbc_queryComboBox);
        queryLabel.setLabelFor(queryComboBox);

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);

        search = new JTable();
        search.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(search);

        JPanel buttons = new JPanel();
        FlowLayout fl_buttons = (FlowLayout) buttons.getLayout();
        fl_buttons.setAlignment(FlowLayout.RIGHT);
        add(buttons, BorderLayout.SOUTH);

        JButton btnSelect = new JButton("Select");
        btnSelect.setEnabled(false);
        btnSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Program program = programPanel.getProgram();
                SearchResult searchResult = searchTableModel.getRow(search.getSelectedRow());
                for (SearchPanelListener listener : listeners) {
                    listener.select(program, searchResult);
                }
            }
        });
        buttons.add(btnSelect);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (SearchPanelListener listener : listeners) {
                    listener.close();
                }
            }
        });
        buttons.add(btnClose);

        search.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = search.getSelectedRow();
                btnSelect.setEnabled(selectedRow != -1);
            }

        });
    }

    public void setProgram(Program program) {
        int info = program.getIntField(ProgramFieldType.INFO_TYPE);
        String episode = program.getTextField(ProgramFieldType.EPISODE_TYPE);

        programPanel.setProgram(program);

        List<String> queries = new ArrayList<>();
        addIgnoreNull(queries, episode);
        addIgnoreNull(queries, program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE));
        queries.addAll(getTitles(program));
        queryComboBox.setModel(new DefaultComboBoxModel<String>(queries.toArray(new String[queries.size()])));

        if ((info & Program.INFO_CATEGORIE_MOVIE) > 0) {
            searchTypes = Arrays.asList(SearchResult.Type.MOVIE);
        } else if ((info & Program.INFO_CATEGORIE_SERIES) > 0) {
            searchTypes = Arrays.asList(SearchResult.Type.EPISODE);
        }

        search();
    }

    protected void search() {
        String query = (String) queryComboBox.getSelectedItem();
        logger.debug("search {}", query);

        if (searchTableModel == null || !Objects.equals(query, searchTableModel.getQuery())) {
            searchTableModel = new SearchTableModel(traktClientService, messageService, query, searchTypes);
            this.search.setModel(searchTableModel);
        }
    }

    public void addListener(SearchPanelListener listener) {
        listeners.add(listener);
    }

}
