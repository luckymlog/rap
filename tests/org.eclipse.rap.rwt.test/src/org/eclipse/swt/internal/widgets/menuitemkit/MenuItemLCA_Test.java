/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.menuitemkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_WIDGET_SELECTED;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.graphics.Graphics;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.internal.protocol.ProtocolTestUtil;
import org.eclipse.rap.rwt.lifecycle.IWidgetAdapter;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.Operation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.json.JSONArray;
import org.json.JSONException;
import org.mockito.ArgumentCaptor;


@SuppressWarnings("deprecation")
public class MenuItemLCA_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Menu menuBar;
  private Menu menu;
  private MenuItemLCA lca;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    menuBar = new Menu( shell, SWT.BAR );
    menu = new Menu( shell, SWT.POP_UP );
    lca = new MenuItemLCA();
    Fixture.fakeNewRequest( display );
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testBarPreserveValues() {
    shell.setMenuBar( menuBar );
    MenuItem menuItem = new MenuItem( menuBar, SWT.BAR );
    Fixture.markInitialized( display );
    testPreserveEnabled( menuItem );
    testPreserveText( menuItem );
  }

  public void testPushPreserveValues() {
    MenuItem fileItem = new MenuItem( menuBar, SWT.CASCADE );
    Menu fileMenu = new Menu( shell, SWT.DROP_DOWN );
    fileItem.setMenu( fileMenu );
    shell.setMenuBar( menuBar );
    MenuItem menuItem = new MenuItem( fileMenu, SWT.PUSH );
    Fixture.markInitialized( display );
    testPreserveEnabled( menuItem );
    testPreserveText( menuItem );
  }

  public void testWidgetSelected() {
    shell.setMenu( menu );
    MenuItem menuItem = new MenuItem( menu, SWT.PUSH );
    SelectionListener listener = mock( SelectionListener.class );
    menuItem.addSelectionListener( listener );

    Fixture.fakeNotifyOperation( getId( menuItem ), ClientMessageConst.EVENT_WIDGET_SELECTED, null );
    Fixture.readDataAndProcessAction( menuItem );

    ArgumentCaptor<SelectionEvent> captor = ArgumentCaptor.forClass( SelectionEvent.class );
    verify( listener, times( 1 ) ).widgetSelected( captor.capture() );
    SelectionEvent event = captor.getValue();
    assertEquals( menuItem, event.getSource() );
    assertEquals( null, event.item );
    assertEquals( SWT.NONE, event.detail );
    assertEquals( 0, event.x );
    assertEquals( 0, event.y );
    assertEquals( 0, event.width );
    assertEquals( 0, event.height );
    assertEquals( true, event.doit );
  }

  public void testCheckItemSelected() {
    shell.setMenu( menu );
    MenuItem menuItem = new MenuItem( menu, SWT.CHECK );

    Fixture.fakeSetParameter( getId( menuItem ), "selection", Boolean.TRUE );
    Fixture.readDataAndProcessAction( menuItem );

    assertTrue( menuItem.getSelection() );
  }

  public void testRadioSelectionEvent() {
    MenuItem menuBarItem = new MenuItem( menuBar, SWT.CASCADE );
    Menu menu = new Menu( menuBarItem );
    menuBarItem.setMenu( menu );
    MenuItem item = new MenuItem( menu, SWT.RADIO );
    SelectionListener listener = mock( SelectionListener.class );
    item.addSelectionListener( listener );

    Fixture.fakeSetParameter( getId( item ), "selection", Boolean.TRUE );
    Fixture.fakeNotifyOperation( getId( item ), ClientMessageConst.EVENT_WIDGET_SELECTED, null );
    Fixture.readDataAndProcessAction( item );

    assertTrue( item.getSelection() );
    verify( listener ).widgetSelected( any( SelectionEvent.class ) );
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=224872
  public void testRadioDeselectionEvent() {
    MenuItem menuBarItem = new MenuItem( menuBar, SWT.CASCADE );
    Menu menu = new Menu( menuBarItem );
    menuBarItem.setMenu( menu );
    MenuItem item = new MenuItem( menu, SWT.RADIO );
    item.setSelection( true );
    SelectionListener listener = mock( SelectionListener.class );
    item.addSelectionListener( listener );

    Fixture.fakeSetParameter( getId( item ), "selection", Boolean.FALSE );
    Fixture.fakeNotifyOperation( getId( item ), ClientMessageConst.EVENT_WIDGET_SELECTED, null );
    Fixture.readDataAndProcessAction( item );

    assertFalse( item.getSelection() );
    verify( listener ).widgetSelected( any( SelectionEvent.class ) );
  }

  public void testRadioTypedSelectionEventOrder_TypedListener() {
    final List<Widget> log = new ArrayList<Widget>();
    MenuItem menuBarItem = new MenuItem( menuBar, SWT.CASCADE );
    Menu menu = new Menu( menuBarItem );
    menuBarItem.setMenu( menu );
    MenuItem radioItem1 = new MenuItem( menu, SWT.RADIO );
    MenuItem radioItem2 = new MenuItem( menu, SWT.RADIO );
    radioItem2.setSelection( true );
    SelectionAdapter listener = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        log.add( event.widget );
      }
    };
    radioItem1.addSelectionListener( listener );
    radioItem2.addSelectionListener( listener );

    Fixture.fakeSetParameter( getId( radioItem1 ), "selection", Boolean.TRUE );
    Fixture.fakeSetParameter( getId( radioItem2 ), "selection", Boolean.FALSE );
    Fixture.fakeNotifyOperation( getId( radioItem1 ), EVENT_WIDGET_SELECTED, null );
    Fixture.fakeNotifyOperation( getId( radioItem2 ), EVENT_WIDGET_SELECTED, null );
    Fixture.readDataAndProcessAction( display );

    assertTrue( Arrays.equals( new Widget[]{ radioItem2, radioItem1 }, log.toArray() ) );
  }

  public void testRadioTypedSelectionEventOrder_UntypedListener() {
    final List<Widget> log = new ArrayList<Widget>();
    MenuItem menuBarItem = new MenuItem( menuBar, SWT.CASCADE );
    Menu menu = new Menu( menuBarItem );
    menuBarItem.setMenu( menu );
    MenuItem radioItem1 = new MenuItem( menu, SWT.RADIO );
    MenuItem radioItem2 = new MenuItem( menu, SWT.RADIO );
    radioItem2.setSelection( true );
    Listener listener = new Listener() {
      public void handleEvent( Event event ) {
        log.add( event.widget );
      }
    };
    radioItem1.addListener( SWT.Selection, listener );
    radioItem2.addListener( SWT.Selection, listener );

    Fixture.fakeSetParameter( getId( radioItem1 ), "selection", Boolean.TRUE );
    Fixture.fakeSetParameter( getId( radioItem2 ), "selection", Boolean.FALSE );
    Fixture.fakeNotifyOperation( getId( radioItem1 ), EVENT_WIDGET_SELECTED, null );
    Fixture.fakeNotifyOperation( getId( radioItem2 ), EVENT_WIDGET_SELECTED, null );
    Fixture.readDataAndProcessAction( display );

    assertTrue( Arrays.equals( new Widget[]{ radioItem2, radioItem1 }, log.toArray() ) );
  }

  public void testArmEvent() {
    MenuItem menuBarItem = new MenuItem( menuBar, SWT.CASCADE );
    Menu menu = new Menu( menuBarItem );
    menuBarItem.setMenu( menu );
    MenuItem pushItem = new MenuItem( menu, SWT.PUSH );
    MenuItem radioItem = new MenuItem( menu, SWT.RADIO );
    MenuItem checkItem = new MenuItem( menu, SWT.CHECK );
    ArmListener pushArmListener = mock( ArmListener.class );
    pushItem.addArmListener( pushArmListener );
    ArmListener radioArmListener = mock( ArmListener.class );
    radioItem.addArmListener( radioArmListener );
    ArmListener checkArmListener = mock( ArmListener.class );
    checkItem.addArmListener( checkArmListener );

    Fixture.fakeNewRequest( display );
    Fixture.fakeNotifyOperation( getId( menu ), ClientMessageConst.EVENT_MENU_SHOWN, null );
    Fixture.readDataAndProcessAction( display );

    verify( pushArmListener, times( 1 ) ).widgetArmed( any( ArmEvent.class ) );
    verify( radioArmListener, times( 1 ) ).widgetArmed( any( ArmEvent.class ) );
    verify( checkArmListener, times( 1 ) ).widgetArmed( any( ArmEvent.class ) );
  }

  private void testPreserveText( MenuItem menuItem ) {
    IWidgetAdapter adapter;
    adapter = WidgetUtil.getAdapter( menuItem );
    Fixture.preserveWidgets();
    assertEquals( "", adapter.getPreserved( Props.TEXT ) );
    Fixture.clearPreserved();
    menuItem.setText( "some text" );
    Fixture.preserveWidgets();
    assertEquals( "some text", adapter.getPreserved( Props.TEXT ) );
    Fixture.clearPreserved();
  }

  private void testPreserveEnabled( MenuItem menuItem ) {
    IWidgetAdapter adapter = WidgetUtil.getAdapter( menuItem );
    Fixture.preserveWidgets();
    assertEquals( Boolean.TRUE, adapter.getPreserved( Props.ENABLED ) );
    Fixture.clearPreserved();
    menuItem.setEnabled( false );
    Fixture.preserveWidgets();
    assertEquals( Boolean.FALSE, adapter.getPreserved( Props.ENABLED ) );
    Fixture.clearPreserved();
    menuItem.setEnabled( true );
    menuItem.getParent().setEnabled( false );
    Fixture.preserveWidgets();
    // even if parent is disabled
    assertEquals( Boolean.TRUE, adapter.getPreserved( Props.ENABLED ) );
    Fixture.clearPreserved();
  }

  public void testRenderCreatePush() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.PUSH );

    lca.renderInitialization( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertEquals( "rwt.widgets.MenuItem", operation.getType() );
    assertTrue( Arrays.asList( operation.getStyles() ).contains( "PUSH" ) );
  }

  public void testRenderCreateCheck() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );

    lca.renderInitialization( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertEquals( "rwt.widgets.MenuItem", operation.getType() );
    assertTrue( Arrays.asList( operation.getStyles() ).contains( "CHECK" ) );
  }

  public void testRenderCreateRadio() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.RADIO );

    lca.renderInitialization( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertEquals( "rwt.widgets.MenuItem", operation.getType() );
    assertTrue( Arrays.asList( operation.getStyles() ).contains( "RADIO" ) );
  }

  public void testRenderCreateCascade() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CASCADE );

    lca.renderInitialization( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertEquals( "rwt.widgets.MenuItem", operation.getType() );
    assertTrue( Arrays.asList( operation.getStyles() ).contains( "CASCADE" ) );
  }

  public void testRenderParent() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.PUSH );

    lca.renderInitialization( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertEquals( WidgetUtil.getId( item.getParent() ), operation.getParent() );
  }

  public void testRenderIndex() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.PUSH );

    lca.renderInitialization( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 0 ), message.findCreateProperty( item, "index" ) );
  }

  public void testRenderDispose() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.PUSH );

    lca.renderDispose( item );

    Message message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( WidgetUtil.getId( item ), operation.getTarget() );
  }

  public void testRenderInitialMenu() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CASCADE );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "menu" ) == -1 );
  }

  public void testRenderMenu() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CASCADE );
    Menu subMenu = new Menu( item );

    item.setMenu( subMenu );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( WidgetUtil.getId( subMenu ), message.findSetProperty( item, "menu" ) );
  }

  public void testRenderMenuUnchanged() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CASCADE );
    Menu subMenu = new Menu( item );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setMenu( subMenu );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "menu" ) );
  }

  public void testRenderInitialEnabled() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.PUSH );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "enabled" ) == -1 );
  }

  public void testRenderEnabled() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.PUSH );

    item.setEnabled( false );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findSetProperty( item, "enabled" ) );
  }

  public void testRenderEnabledUnchanged() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.PUSH );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setEnabled( false );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "enabled" ) );
  }

  public void testRenderInitialSelection() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "selection" ) == -1 );
  }

  public void testRenderSelection() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );

    item.setSelection( true );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findSetProperty( item, "selection" ) );
  }

  public void testRenderSelectionUnchanged() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setSelection( true );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "selection" ) );
  }

  public void testRenderInitialCustomVariant() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "customVariant" ) == -1 );
  }

  public void testRenderCustomVariant() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );

    item.setData( RWT.CUSTOM_VARIANT, "blue" );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( item, "customVariant" ) );
  }

  public void testRenderCustomVariantUnchanged() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setData( RWT.CUSTOM_VARIANT, "blue" );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "customVariant" ) );
  }

  public void testRenderInitialTexts() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "text" ) == -1 );
  }

  public void testRenderTexts() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );

    item.setText( "foo" );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( item, "text" ) );
  }

  public void testRenderTextsUnchanged() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );

    item.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "text" ) );
  }

  public void testRenderInitialImages() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );

    lca.render( item );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( item );
    assertTrue( operation.getPropertyNames().indexOf( "image" ) == -1 );
  }

  public void testRenderImages() throws IOException, JSONException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Image image = Graphics.getImage( Fixture.IMAGE1 );

    item.setImage( image );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( item, "image" );
    String expected = "[\"rwt-resources/generated/90fb0bfe\",58,12]";
    assertTrue( ProtocolTestUtil.jsonEquals( expected, actual ) );
  }

  public void testRenderImagesUnchanged() throws IOException {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Image image = Graphics.getImage( Fixture.IMAGE1 );

    item.setImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( item, "image" ) );
  }

  public void testRenderAddSelectionListener() throws Exception {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Fixture.preserveWidgets();

    item.addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( item, "selection" ) );
  }

  public void testRenderRemoveSelectionListener() throws Exception {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    SelectionListener listener = new SelectionAdapter() { };
    item.addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Fixture.preserveWidgets();

    item.removeSelectionListener( listener );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( item, "selection" ) );
  }

  public void testRenderSelectionListenerUnchanged() throws Exception {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Fixture.preserveWidgets();

    item.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( item, "selection" ) );
  }

  public void testRenderAddHelpListener() throws Exception {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Fixture.preserveWidgets();

    item.addHelpListener( new HelpListener() {
      public void helpRequested( HelpEvent e ) {
      }
    } );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( item, "help" ) );
  }

  public void testRenderRemoveHelpListener() throws Exception {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    HelpListener listener = new HelpListener() {
      public void helpRequested( HelpEvent e ) {
      }
    };
    item.addHelpListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Fixture.preserveWidgets();

    item.removeHelpListener( listener );
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( item, "help" ) );
  }

  public void testRenderHelpListenerUnchanged() throws Exception {
    MenuItem item = new MenuItem( menu, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( item );
    Fixture.preserveWidgets();

    item.addHelpListener( new HelpListener() {
      public void helpRequested( HelpEvent e ) {
      }
    } );
    Fixture.preserveWidgets();
    lca.renderChanges( item );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( item, "help" ) );
  }
}
