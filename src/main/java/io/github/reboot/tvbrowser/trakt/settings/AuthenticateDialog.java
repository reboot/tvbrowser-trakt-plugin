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

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import io.github.reboot.tvbrowser.trakt.client.AuthenticationCallback;
import io.github.reboot.tvbrowser.trakt.client.AuthenticationContext;
import io.github.reboot.tvbrowser.trakt.client.TraktClientService;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import util.browserlauncher.Launch;

class AuthenticateDialog extends JDialog implements AuthenticationCallback {

    private static final long serialVersionUID = 1L;

    private final SettingsService settingsService;

    private final TraktClientService traktClientService;

    private final ScheduledExecutorService executorService;

    private final MessageService messageService;

    private final JLabel timer;

    private final JButton closeButton;

    private AuthenticationContext authenticationContext;

    private Future<?> timeoutTask;
    private JEditorPane status;

    AuthenticateDialog(SettingsService settingsService,
                       TraktClientService traktClientService,
                       ScheduledExecutorService executorService,
                       MessageService messageService) {
        this.settingsService = settingsService;
        this.traktClientService = traktClientService;
        this.executorService = executorService;
        this.messageService = messageService;

        setTitle(messageService.getMessage("settings.authenticate.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setBounds(0, 0, 480, 360);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWeights = new double[] { 1.0 };
        gbl_contentPanel.rowWeights = new double[] { 1.0, 0.0 };
        contentPanel.setLayout(gbl_contentPanel);
        {
            status = new JEditorPane();
            status.setBackground(new Color(0, 0, 0, 0));
            status.addHyperlinkListener(new HyperlinkListener() {

                public void hyperlinkUpdate(HyperlinkEvent event) {
                    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        Launch.openURL(event.getURL().toString());
                    }
                }

            });
            status.setContentType("text/html");
            status.setEditable(false);
            GridBagConstraints gbc_status = new GridBagConstraints();
            gbc_status.weighty = 1.0;
            gbc_status.weightx = 1.0;
            gbc_status.insets = new Insets(0, 0, 4, 0);
            gbc_status.fill = GridBagConstraints.BOTH;
            gbc_status.gridx = 0;
            gbc_status.gridy = 0;
            contentPanel.add(status, gbc_status);
        }
        {
            timer = new JLabel("");
            timer.setHorizontalAlignment(SwingConstants.CENTER);
            GridBagConstraints gbc_timer = new GridBagConstraints();
            gbc_timer.insets = new Insets(0, 4, 0, 4);
            gbc_timer.anchor = GridBagConstraints.LINE_END;
            gbc_timer.gridx = 0;
            gbc_timer.gridy = 1;
            contentPanel.add(timer, gbc_timer);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                closeButton = new JButton("Abort");
                closeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
                closeButton.setActionCommand("OK");
                buttonPane.add(closeButton);
                getRootPane().setDefaultButton(closeButton);
            }
        }
    }

    @Override
    public void dispose() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            timeoutTask = null;
        }
        if (authenticationContext != null) {
            authenticationContext.abort();
            authenticationContext = null;
        }
        super.dispose();
    }

    void start() {
        authenticationContext = traktClientService.authenticate(this);
    }

    @Override
    public void code(String code, String verificationURL, int expiresIn) {
        status.setText(messageService.getMessage("settings.authenticate.code", verificationURL, escapeHtml4(verificationURL), code));
        AtomicInteger remainingTime = new AtomicInteger();
        remainingTime.set(expiresIn + 1);
        timeoutTask = executorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                int value = remainingTime.decrementAndGet();
                if (value > 0) {
                    timer.setText("Timeout in " + value + " seconds");
                }
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void success(String accessToken, String refreshToken) {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }
        status.setText(messageService.getMessage("settings.authenticate.success"));
        timer.setText("");
        closeButton.setText("OK");

        settingsService.setAccessToken(accessToken, refreshToken, false);
    }

    @Override
    public void failed(String error) {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }
        status.setText("Failure: " + error);
        timer.setText("");
        closeButton.setText("Close");
    }

    @Override
    public void timeout() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }
        status.setText("Timeout");
        timer.setText("");
        closeButton.setText("Close");
    }
}
