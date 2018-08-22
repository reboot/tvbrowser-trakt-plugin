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
package io.github.reboot.tvbrowser.trakt.settings;

import io.github.reboot.tvbrowser.trakt.client.TraktClientService;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

class SettingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final SettingsService settingsService;

    private final TraktClientService traktClientService;

    private final ScheduledExecutorService executorService;

    private final MessageService messageService;

    private JTextField account;

    SettingsPanel(SettingsService settingsService,
                  TraktClientService traktClientService,
                  ScheduledExecutorService executorService,
                  MessageService messageService) {
        this.settingsService = settingsService;
        this.traktClientService = traktClientService;
        this.executorService = executorService;
        this.messageService = messageService;

        setBorder(null);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0 };
        gridBagLayout.rowWeights = new double[] { 0.0, 1.0 };
        setLayout(gridBagLayout);

        JLabel accountLabel = new JLabel("Account");
        accountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_accountLabel = new GridBagConstraints();
        gbc_accountLabel.insets = new Insets(4, 0, 0, 0);
        gbc_accountLabel.gridx = 0;
        gbc_accountLabel.gridy = 0;
        add(accountLabel, gbc_accountLabel);

        account = new JTextField();
        account.setEditable(false);
        account.setColumns(10);
        GridBagConstraints gbc_account = new GridBagConstraints();
        gbc_account.weightx = 1.0;
        gbc_account.insets = new Insets(4, 4, 0, 4);
        gbc_account.fill = GridBagConstraints.HORIZONTAL;
        gbc_account.gridx = 1;
        gbc_account.gridy = 0;
        add(account, gbc_account);

        JButton login = new JButton("Login");
        login.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                login();
            }

        });
        GridBagConstraints gbc_login = new GridBagConstraints();
        gbc_login.insets = new Insets(4, 0, 0, 0);
        gbc_login.gridx = 2;
        gbc_login.gridy = 0;
        add(login, gbc_login);

        JPanel filler = new JPanel();
        filler.setBorder(null);
        GridBagConstraints gbc_filler = new GridBagConstraints();
        gbc_filler.gridwidth = 3;
        gbc_filler.fill = GridBagConstraints.BOTH;
        gbc_filler.gridx = 0;
        gbc_filler.gridy = 1;
        add(filler, gbc_filler);
    }

    void init() {
        account.setText(traktClientService.getAccount());
    }

    void login() {
        AuthenticateDialog authenticateDialog = new AuthenticateDialog(settingsService, traktClientService, executorService, messageService);
        authenticateDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                init();
            }

        });
        authenticateDialog.setLocationRelativeTo(this);
        authenticateDialog.start();
        authenticateDialog.setVisible(true);
    }

}
