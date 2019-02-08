/*******************************************************************************
 * Copyright (c) 2017, 2019 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.libraries.ui.move;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Priority;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.ui.ImageURIRegistry;
import org.eclipse.emf.common.ui.dialogs.DiagnosticDialog;
import org.eclipse.emf.common.ui.viewer.ColumnViewerInformationControlToolTipSupport;
import org.eclipse.emf.common.ui.viewer.IUndecoratingLabelProvider;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedImage;
import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.DecoratingColumLabelProvider;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.emf.transaction.ExceptionHandler;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.polarsys.capella.common.flexibility.wizards.ui.FlexibilityColors;
import org.polarsys.capella.common.helpers.EcoreUtil2;
import org.polarsys.capella.common.libraries.IModel;
import org.polarsys.capella.common.libraries.manager.LibraryManager;
import org.polarsys.capella.common.libraries.manager.LibraryManagerExt;
import org.polarsys.capella.common.tools.report.util.LogExt;
import org.polarsys.capella.common.tools.report.util.ReportManagerDefaultComponents;
import org.polarsys.capella.core.data.core.validation.constraint.ReferentialConstraintsResourceSetListener;
import org.polarsys.capella.core.libraries.model.ICapellaModel;
import org.polarsys.capella.core.libraries.ui.Activator;
import org.polarsys.capella.core.model.helpers.move.CapellaMoveHelper;
import org.polarsys.capella.core.model.helpers.move.Stage;
import org.polarsys.capella.core.model.helpers.move.StageListener;
import org.polarsys.capella.core.platform.sirius.ui.navigator.actions.LocateInCapellaExplorerAction;
import org.polarsys.capella.core.platform.sirius.ui.navigator.drop.ExplorerDropAdapterAssistant;
import org.polarsys.capella.core.platform.sirius.ui.navigator.view.CapellaCommonNavigator;

import com.google.common.collect.Lists;

public class MoveStagingView extends ViewPart implements ISelectionProvider, ITabbedPropertySheetPageContributor {

  /**
   * The context menu id for the stage viewer
   */
  public final static String STAGEVIEWER_CONTEXT_MENU = "org.polarsys.capella.core.libraries.ui.moveview.stageViewer"; //$NON-NLS-1$
  
  /**
   * The context menu id for the destination viewer
   */
  public final static String DESTINATIONVIEWER_CONTEXT_MENU = "org.polarsys.capella.core.libraries.ui.moveview.destinationViewer"; //$NON-NLS-1$

  TreeViewer stageViewer;
  TreeViewer destinationViewer;
  ColumnViewerInformationControlToolTipSupport tooltipSupport;

  FormToolkit toolkit;

  final ComposedAdapterFactory cfac = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

  Stage stage;
  StageListener listener;

  Form form;

  Section stageSection;
  Section destinationSection;

  /**
   * This executes the transfer
   */
  IAction executeAction;

  /**
   * Actions on the left viewer
   */
  IAction addRequiredAction;
  IAction addAllRequiredAction;
  IAction unstageAction;

  /**
   * Actions on the right viewer
   */
  IAction clearParentAction;
  
  public Stage getStage(){
    return stage;
  }

  @Override
  public void createPartControl(Composite parent) {

    toolkit = new FormToolkit(parent.getDisplay());
    form = toolkit.createForm(parent);

    GridLayout layout = new GridLayout(2, true);
    form.getBody().setLayout(layout);

    stageSection = toolkit.createSection(form.getBody(),
        Section.TITLE_BAR|Section.DESCRIPTION|
        Section.EXPANDED);
    stageSection.setText(Messages.MoveStagingView_stageSectionTitle);
    stageSection.setDescription(Messages.MoveStagingView_stageSectionDescription);

    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    stageSection.setLayoutData(gd);

    Composite stageSectionClient = toolkit.createComposite(stageSection);
    stageSectionClient.setLayout(new GridLayout(2, false));
    stageSection.setClient(stageSectionClient);
    createStageViewer(stageSectionClient);

    destinationSection = toolkit.createSection(form.getBody(),
        Section.TITLE_BAR|Section.DESCRIPTION|
        Section.EXPANDED);
    destinationSection.setText(Messages.MoveStagingView_destinationSectionTitle);
    destinationSection.setDescription(Messages.MoveStagingView_destinationSectionDescription);

    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    destinationSection.setLayoutData(gd);

    Composite destinationSectionClient = toolkit.createComposite(destinationSection);
    destinationSectionClient.setLayout(new GridLayout(2, false));
    destinationSection.setClient(destinationSectionClient);
    createDestinationViewer(destinationSectionClient);

    getViewSite().setSelectionProvider(this);

    initTreePacker();
    initSelectionSynchronizer();

    initToolbar();

  }

  private void initToolbar() {
    ImageDescriptor descriptor = DebugPluginImages.getImageRegistry().getDescriptor(IDebugUIConstants.IMG_ACT_RUN);
    executeAction = new Action("", descriptor) { //$NON-NLS-1$

    @Override
    public void run() {
      AtomicBoolean forcedMove = new AtomicBoolean();
      AtomicReference<Diagnostic> referenceErrors = new AtomicReference<>();
      destinationViewer.getTree().setRedraw(false);
      ResourceSetListener validateListener = new ReferentialConstraintsResourceSetListener(status -> {
        referenceErrors.set(status);
        MyDiagnosticDialog dialog = new MyDiagnosticDialog(getViewSite().getShell(), status); 
        if (dialog.open() == Window.CANCEL) {
          throw new RollbackException(new Status(IStatus.CANCEL, Activator.PLUGIN_ID, Messages.MoveStagingView_CancelStatusMessage));
        } else {
          forcedMove.set(true);
        }
      });
      TransactionalEditingDomain domain = (TransactionalEditingDomain) stage.getEditingDomain();
      domain.addResourceSetListener(validateListener);
      Diagnostic result = null;
      try {
        result = stage.executeWithDiagnostics();
        report(result);
        if (result.getSeverity() == Diagnostic.OK) {
          boolean resetView = false;
          Diagnostic referenceErrorDiagnostic = referenceErrors.get();
          if (referenceErrorDiagnostic != null) {
            report(referenceErrorDiagnostic);
            String userDecision = null;
            if (forcedMove.get()) {
              userDecision = Messages.MoveStagingView_forced_move_message;
              resetView = true;
            } else {
              userDecision = Messages.MoveStagingView_CancelStatusMessage;
            }
            report(new BasicDiagnostic(Activator.PLUGIN_ID, 0, userDecision, new Object[0]));
          } else {
            resetView = true;
          }
          if (resetView) {
            reset();
          } 
        } else {
          DiagnosticDialog.open(getViewSite().getShell(), Messages.MoveStagingView_fail_dialog_title, Messages.MoveStagingView_fail_dialog_message, result);
        }
      } finally {
        domain.removeResourceSetListener(validateListener);
        destinationViewer.getTree().setRedraw(true);
      }
    }
    };

    executeAction.setEnabled(false);
    getViewSite().getActionBars().getToolBarManager().add(executeAction);
  }


  private void report(Diagnostic d) {
    Priority prio = LogExt.convertSeverityToPriority(d);
    if (d.getChildren().isEmpty()) {
      ReportManagerDefaultComponents.getReportManagerForModel().log(prio, d);
    } else {
      for (Diagnostic child : d.getChildren()) {
        report(child);
      }
    }
  }

  // Mirrors selection of staged elements in the left view to the same elements in the right view and vice versa
  private void initSelectionSynchronizer() {

    ISelectionChangedListener listener = new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        TreeViewer target = null;
        if (((TreeViewer)event.getSelectionProvider()).getTree().isFocusControl()) {
          if (event.getSelectionProvider() == stageViewer) {
            target = destinationViewer;
          } else {
            target = stageViewer;
          }
          if (((IStructuredSelection) event.getSelection()).size() == 1) {
            Object elem = ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (stage.getElements().contains(elem)) {
              target.setSelection(event.getSelection());
              target.reveal(elem);
            }
          }
        }
      }
    };
    stageViewer.addSelectionChangedListener(listener);
    destinationViewer.addSelectionChangedListener(listener);

  }

  private void initTreePacker() {
    TreeListener packer = new TreeListener() {
      private void pack(final TreeEvent e) {
        Display.getCurrent().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (!e.widget.isDisposed()) {
              ((Tree)e.widget).getColumn(0).pack();
            }
          }
        });
      }

      @Override
      public void treeExpanded(final TreeEvent e) {
        pack(e);
      }

      @Override
      public void treeCollapsed(final TreeEvent e) {
        pack(e);
      }
    };
    stageViewer.getTree().addTreeListener(packer);
    destinationViewer.getTree().addTreeListener(packer);
  }

  private void createStageViewer(Composite stageSectionClient) {
    stageViewer = new TreeViewer(toolkit.createTree(stageSectionClient, SWT.MULTI | SWT.BORDER));

    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    stageViewer.getTree().setLayoutData(gd);

    TreeColumn column = new TreeColumn(stageViewer.getTree(), SWT.LEFT);
    column.setWidth(200);

    stageViewer.setAutoExpandLevel(3);

    stageViewer.setContentProvider(new AdapterFactoryContentProvider(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE)){

      @Override
      public Object[] getElements(Object object) {
        Set<Object> elems = new LinkedHashSet<Object>();
        for (EObject e : stage.getElements()){
          elems.add(EcoreUtil.getRootContainer(e));
        }
        return elems.toArray();
      }

      @Override
      public Object getParent(Object object) {
        return ((EObject) object).eContainer();
      }

    });


    stageViewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, new ViewerDropAdapter(stageViewer) {

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {

        ISelection sel =  LocalSelectionTransfer.getTransfer().getSelection();

        if (sel instanceof IStructuredSelection){

          Collection<EObject> eObjects = new ArrayList<EObject>();

          for (Iterator<?> it = ((IStructuredSelection) sel).iterator(); it.hasNext();) {
            Object next = it.next();
            if (next instanceof EObject) {
              eObjects.add((EObject) next);
            } else {
              return false;
            }
          }

          EditingDomain editingDomain = EcoreUtil2.getEditingDomain(eObjects);
          if (editingDomain == null) {
            return false;
          }

          if (stage == null || stage.getEditingDomain() == editingDomain && Collections.disjoint(stage.getElements(), eObjects)){
            return true;
          }
        }

        return false;
      }

      @Override
      public boolean performDrop(Object data) {

        // validate() ensures that this is safe
        @SuppressWarnings("unchecked") Collection<EObject> dropped = ((IStructuredSelection) data).toList();

        if (stage == null){

          // the first element is dropped onto the staging area

          stage = new Stage(EcoreUtil2.getEditingDomain(dropped));
          listener = new StageListener() {

            @Override
            public void stageChanged(Stage s) {
              PlatformUI.getWorkbench().getDisplay().asyncExec(() -> handleStageChanged(s));
            }

            @Override
            public void elementsAdded(Collection<EObject> elements) {
              PlatformUI.getWorkbench().getDisplay().asyncExec(() -> handleStageElementsAdded(elements));
            }

            @Override
            public void parentChanged(EObject staged, EObject oldParent, EObject newParent) {
              PlatformUI.getWorkbench().getDisplay().asyncExec(() -> handleStageParentChanged(staged, oldParent, newParent));
            }

            @Override
            public void elementsRemoved(Collection<? extends EObject> elements) {
              PlatformUI.getWorkbench().getDisplay().asyncExec(() -> handleStageElementsRemoved(elements));
            }
          };
          stage.addStageListener(listener);
          stageViewer.setInput(stage.getEditingDomain().getResourceSet());
          stageViewer.setLabelProvider(
              new DecoratingColumLabelProvider(new LabelProvider(cfac, stageViewer), new StageLabelDecorator()));
          stageViewer.setFilters(new CollectionTreeFilter(stage.getElements(), false));
          tooltipSupport = new ColumnViewerInformationControlToolTipSupport(stageViewer, new DiagnosticDecorator.EditingDomainLocationListener(stage.getEditingDomain(), stageViewer) {
            @Override
            protected void setSelection(final Object object) {
              MyLocateInCapellaExplorerAction action = new MyLocateInCapellaExplorerAction();
              action.selectElementInCapellaExplorer(new StructuredSelection(object));
            }
          });

          destinationViewer.setInput(stage.getEditingDomain().getResourceSet());

        }

        stage.addAll(dropped);
        return true;
      }

    });

    stageViewer.addDragSupport(0, new Transfer[] { LocalSelectionTransfer.getTransfer() } , new ViewerDragAdapter(stageViewer){

      @Override
      public void dragStart(DragSourceEvent event) {
        if (stage.getElements().containsAll(stageViewer.getStructuredSelection().toList())){
          super.dragStart(event);
        } else {
          event.doit = false;
        }
      }

      @Override
      public void dragFinished(DragSourceEvent event) {
        // when the element is dropped on the right viewer, paint it in green on the left
        super.dragFinished(event);
        stageViewer.setSelection(StructuredSelection.EMPTY);
        stageViewer.refresh();
      }

    });

    ToolBarManager stageViewerToolBarManager = new ToolBarManager(SWT.FLAT | SWT.VERTICAL);
    ToolBar stageViewerToolbar = stageViewerToolBarManager.createControl(stageSectionClient);
    gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
    stageViewerToolbar.setLayoutData(gd);

    unstageAction = new UnstageAction();
    addRequiredAction = new AddRequiredAction();
    addAllRequiredAction = new AddAllRequiredAction();

    stageViewer.addSelectionChangedListener((ISelectionChangedListener) addRequiredAction);
    stageViewer.addSelectionChangedListener((ISelectionChangedListener) unstageAction);
    stageViewer.addSelectionChangedListener((ISelectionChangedListener) addAllRequiredAction);

    stageViewerToolBarManager.add(unstageAction);
    stageViewerToolBarManager.add(addRequiredAction);
    stageViewerToolBarManager.add(addAllRequiredAction);

    stageViewer.setSelection(StructuredSelection.EMPTY);

    stageViewerToolBarManager.update(true);

    MenuManager stageContextMenu = new MenuManager();
    stageContextMenu.setRemoveAllWhenShown(true);
    stageContextMenu.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager manager) {
        if (unstageAction.isEnabled()) {
          manager.add(unstageAction);
        }
        if (addRequiredAction.isEnabled()) {
          manager.add(addRequiredAction);
        }
        if (addAllRequiredAction.isEnabled()) {
          manager.add(addAllRequiredAction);
        }
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });

    stageViewer.getControl().setMenu(stageContextMenu.createContextMenu(stageViewer.getControl()));
    getViewSite().registerContextMenu(STAGEVIEWER_CONTEXT_MENU, stageContextMenu, stageViewer);

  }

  private void createDestinationViewer(Composite destinationSectionClient) {
    destinationViewer = new TreeViewer(toolkit.createTree(destinationSectionClient, SWT.BORDER | SWT.MULTI));
    TreeColumn column2 = new TreeColumn(destinationViewer.getTree(), SWT.LEFT);
    column2.setWidth(200);

    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    destinationViewer.getControl().setLayoutData(gd);

    destinationViewer.setAutoExpandLevel(3);

    destinationViewer.setLabelProvider(
        new DecoratingColumLabelProvider(new LabelProvider(cfac, destinationViewer), new StageLabelDecorator()));
    destinationViewer.setContentProvider(new AdapterFactoryContentProvider(cfac) {
      @Override
      public Object[] getElements(Object object) {
        List<Object> result = new ArrayList<Object>();
        TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(object);
        IModel model = LibraryManager.INSTANCE.getModel(domain);
        for (IModel lib : LibraryManagerExt.getAllReferences(model)) {
          if (lib instanceof ICapellaModel) {
            result.add(((ICapellaModel)lib).getProject(domain));
          }
        }
        return result.toArray();
      }

      @Override
      public boolean hasChildren(Object object) {
        for (Object o : stage.getElements()) {
          if (stage.getNewParent((EObject)o) == object) {
            return true;
          }
        }
        return super.hasChildren(object);
      }

      @Override
      public Object[] getChildren(Object element) {

        List<Object> newChildren = null;

        for (EObject e : stage.getElements()) {
          if (element == stage.getNewParent(e)) {
            if (newChildren == null) {
              newChildren = new ArrayList<Object>();
            }
            newChildren.add(e);
          }
        }

        List<Object> result = Lists.newArrayList(super.getChildren(element));

        // during execution the stage model isn't updated so take care to not
        // return a moved element twice. also, staged elements are always the
        // 'last' children.
        if (newChildren != null) {
          for (Iterator<Object> it = result.iterator(); it.hasNext();) {
            Object next = it.next();
            if (newChildren.contains(next)) {
              it.remove();
            }
          }
          result.addAll(newChildren);
        }

        return result.toArray();
      }

    });

    destinationViewer.addDropSupport(0, new Transfer[] { LocalSelectionTransfer.getTransfer() }, new ViewerDropAdapter(destinationViewer) {

      final ExplorerDropAdapterAssistant a = new ExplorerDropAdapterAssistant(new CapellaMoveHelper());

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {

        if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {

          // only accept drops from this same view
          IWorkbenchPart active = MoveStagingView.this.getSite().getPage().getActivePart();
          if (active == MoveStagingView.this) {
            ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();

            if (sel instanceof IStructuredSelection){
              Collection<?> selected = ((IStructuredSelection)sel).toList();
              if (stage != null && stage.getElements().containsAll(selected)){
                return a.validateDrop(target, operation, transferType).isOK();
              }
            }
          }
        }
        return false;
      }

      @Override
      public boolean performDrop(Object data) {

        @SuppressWarnings("unchecked") Collection<EObject> staged = (((IStructuredSelection) data).toList());
        for (EObject e : staged) {
          stage.setParent(e, (EObject) getCurrentTarget());
        }

        return true;
      }
    });

    destinationViewer.addDragSupport(0, new Transfer[] { LocalSelectionTransfer.getTransfer() }, new ViewerDragAdapter(destinationViewer){

      @Override
      public void dragStart(DragSourceEvent event) {
        if (!stage.getElements().containsAll(((IStructuredSelection) viewer.getSelection()).toList())) {
          event.doit = false;
        }
        super.dragStart(event);
      }

    });

    ToolBarManager destinationViewerToolBarManager = new ToolBarManager(SWT.FLAT | SWT.VERTICAL);
    ToolBar destinationViewerToolbar = destinationViewerToolBarManager.createControl(destinationSectionClient);
    gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
    destinationViewerToolbar.setLayoutData(gd);

    clearParentAction = new ClearParentAction();

    destinationViewer.addSelectionChangedListener((ISelectionChangedListener) clearParentAction);

    destinationViewerToolBarManager.add(clearParentAction);
    destinationViewerToolBarManager.update(true);

    MenuManager destinationContextMenu = new MenuManager();
    destinationContextMenu.setRemoveAllWhenShown(true);
    destinationContextMenu.addMenuListener(new IMenuListener() {

      @Override
      public void menuAboutToShow(IMenuManager manager) {
        if (clearParentAction.isEnabled()) {
          manager.add(clearParentAction);
        }
        destinationContextMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });

    destinationViewer.setSelection(StructuredSelection.EMPTY);
    
    destinationViewer.getControl().setMenu(destinationContextMenu.createContextMenu(destinationViewer.getControl()));
    getViewSite().registerContextMenu(DESTINATIONVIEWER_CONTEXT_MENU, destinationContextMenu, destinationViewer);
  }


  @Override
  public void setFocus() {
    stageViewer.getControl().setFocus();
  }

  private void handleStageChanged(Stage s) {
    stageViewer.refresh(true);
    destinationViewer.refresh(true);
    stageViewer.getTree().getColumn(0).pack();
    updateActions();
  }

  private void handleStageElementsAdded(Collection<EObject> elements) {

    handleStageChanged(stage);

    for (EObject e: elements) {
      stageViewer.reveal(e);
    }

    for (Iterator<EObject> it = EcoreUtil.getAllContents(elements); it.hasNext();) {
      EObject next = it.next();
      if (stage.hasBackreferences(next)) {
        stageViewer.reveal(next);
      }
    }

    stageViewer.getTree().getColumn(0).pack();

  }

  private void handleStageParentChanged(EObject staged, EObject oldParent, EObject newParent) {
    if (oldParent != null) {
      destinationViewer.refresh(oldParent, true);
    }
    destinationViewer.refresh(newParent, true);
    if (newParent != null) {
      destinationViewer.setExpandedState(newParent, true);
    } else {
      stageViewer.update(staged, null);
    }
    destinationViewer.getTree().getColumn(0).pack();
    updateActions();
  }

  private void handleStageElementsRemoved(Collection<? extends EObject> elements) {
    stageViewer.refresh(true);
    destinationViewer.refresh(true);
    stageViewer.getTree().getColumn(0).pack();
    destinationViewer.getTree().getColumn(0).pack();

    updateActions();
  }

  private void updateActions() {
    executeAction.setEnabled(stage != null && stage.canExecute());

    ((BaseSelectionListenerAction)unstageAction).selectionChanged(stageViewer.getStructuredSelection());
    ((BaseSelectionListenerAction)addRequiredAction).selectionChanged(stageViewer.getStructuredSelection());
    ((BaseSelectionListenerAction)addAllRequiredAction).selectionChanged(stageViewer.getStructuredSelection());

    ((BaseSelectionListenerAction)clearParentAction).selectionChanged(destinationViewer.getStructuredSelection());
  }

  private class AbstractDeleteAction extends BaseSelectionListenerAction {

    @Deprecated // use CommandImageConstants from 1.4 on 
    private static final String DELETE_IMAGE = "org.polarsys.capella.common.re.subcommands.delete"; //$NON-NLS-1$

    protected AbstractDeleteAction(String text) {
      super(text);
      ICommandImageService service = getViewSite().getService(ICommandImageService.class);
      setImageDescriptor(service.getImageDescriptor(DELETE_IMAGE));
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
      return stage != null && !selection.isEmpty() && stage.getElements().containsAll(selection.toList());
    }
    
  }
  
  private final class ClearParentAction extends AbstractDeleteAction {

    private ClearParentAction() {
      super(Messages.MoveStagingView_clearParentLabel);
      setToolTipText(Messages.MoveStagingView_clearParentLabel);
    }

    @Override
    public void run() {
      for (Object e : destinationViewer.getStructuredSelection().toList()) {
        stage.setParent((EObject) e, null);
      }
    }

  }

  private class UnstageAction extends AbstractDeleteAction {
    private UnstageAction() {
      super(Messages.MoveStagingView_unstageLabel);
      setToolTipText(Messages.MoveStagingView_unstageLabel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        stage.removeAll(getStructuredSelection().toList());
    }

  }

  private abstract class AbstractAddRequiredAction extends BaseSelectionListenerAction {

    protected AbstractAddRequiredAction(String text) {
      super(text);
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
      boolean enabled = false;
      for (Iterator<?> it = selection.iterator(); it.hasNext();) {
        EObject next = (EObject) it.next();
        if (stage != null && stage.getBackreferences(next).size() > 0) {
          enabled = true;
          break;
        }
      }
      return enabled;
    }

  }

  private class AddRequiredAction extends AbstractAddRequiredAction {

    private AddRequiredAction() {
      super(Messages.MoveStagingView_addRequiredElementsLabel);
      setImageDescriptor(Activator.getDefault().getImageDescriptor("full/etool16/add_dependencies.gif")); //$NON-NLS-1$
      setToolTipText(Messages.MoveStagingView_addRequiredElementsLabel);
    }

    @Override
    public void run() {
        Collection<EObject> toAdd = new LinkedHashSet<EObject>();
        for (Iterator<?> it = getStructuredSelection().iterator(); it.hasNext();) {
          EObject next = (EObject) it.next();
          toAdd.addAll(stage.getBackreferences(next).values());
        }
        stage.addAll(toAdd);
    }

  }

  private class AddAllRequiredAction extends AbstractAddRequiredAction {

    protected AddAllRequiredAction() {
      super(Messages.MoveStagingView_addAllRequiredElementsLabel);
      setImageDescriptor(Activator.getDefault().getImageDescriptor("full/etool16/add_alldependencies.gif")); //$NON-NLS-1$
      setToolTipText(Messages.MoveStagingView_addAllRequiredElementsLabel);
    }

    public void run() {

      @SuppressWarnings("unchecked") 
      Collection<EObject> input = getStructuredSelection().toList();

      while (!input.isEmpty()) {
        Collection<EObject> backrefs = new LinkedHashSet<EObject>();
        for (EObject e : input) {
          backrefs.addAll(stage.getBackreferences(e).values());
        }
        stage.addAll(backrefs);
        input = backrefs;
      }
    }

  }


  class StageLabelDecorator extends CellLabelProvider implements ILabelDecorator {

    @Override
    public Image decorateImage(Image image, Object element) {

      Collection<Diagnostic> diagnostic = stage.getDiagnostics((EObject) element);

      for (Diagnostic diag : diagnostic) {
        if (diagnostic != null && diag.getSeverity() >= Diagnostic.WARNING)
        {
          return decorate(image, diag);
        }
      }
      return image;
    }

    public Image decorate(Image image, Diagnostic diagnostic) {
      List<Object> images = new ArrayList<Object>(2);
      images.add(image);
      images.add(EMFEditUIPlugin.INSTANCE.getImage(diagnostic.getSeverity() == Diagnostic.WARNING ? "full/ovr16/warning_ovr.gif" : "full/ovr16/error_ovr.gif")); //$NON-NLS-1$ //$NON-NLS-2$
      ComposedImage composedImage = new ComposedImage(images);
      return ExtendedImageRegistry.INSTANCE.getImage(composedImage);
    }

    @Override
    public String decorateText(String text, Object element) {
      return text;
    }

    protected void buildToolTipMessage(StringBuilder result, ILabelProvider labelProvider, Object object, Diagnostic diagnostic)
    {
      String message = diagnostic.getMessage();
      ImageDescriptor imageDescriptor =
        ExtendedImageRegistry.INSTANCE.getImageDescriptor
          (EMFEditUIPlugin.INSTANCE.getImage(diagnostic.getSeverity() == Diagnostic.WARNING ? "full/ovr16/warning_ovr.gif" : "full/ovr16/error_ovr.gif")); //$NON-NLS-1$ //$NON-NLS-2$

      EObject target = (EObject) diagnostic.getData().get(1); // the referenced object
      EReference ref = (EReference) diagnostic.getData().get(2); // the reference

      result.append("<div>"); //$NON-NLS-1$
      String sourceText = DiagnosticDecorator.escapeContent(labelProvider.getText(object));
      URI sourceImage = ImageURIRegistry.INSTANCE.getImageURI(labelProvider.getImage(object));
      String targetText = DiagnosticDecorator.escapeContent(labelProvider.getText(target));
      URI targetImage = ImageURIRegistry.INSTANCE.getImageURI(labelProvider.getImage(target));

      result.append(sourceText);
      result.append(String.format(Messages.MoveStagingView_backrefTooltip, sourceImage, EcoreUtil.getURI(target), targetText, targetImage, DiagnosticDecorator.escapeContent(labelProvider.getText(ref))));
      result.append("</div>"); //$NON-NLS-1$
    }

    @Override
    public String getToolTipText(Object element) {
      Collection<Diagnostic> diags = stage.getDiagnostics((EObject) element);
      if (diags != null && diags.size() > 0) {
        Diagnostic d = diags.iterator().next();
        StringBuilder builder = new StringBuilder();

        ILabelProvider labelProvider = (ILabelProvider) stageViewer.getLabelProvider();
        if (labelProvider instanceof IUndecoratingLabelProvider)
        {
          final IUndecoratingLabelProvider undecoratingLabelProvider = (IUndecoratingLabelProvider)labelProvider;
          labelProvider =
            new ILabelProvider()
            {
              @Override
              public void removeListener(ILabelProviderListener listener)
              {
                undecoratingLabelProvider.removeListener(listener);
              }

              @Override
              public boolean isLabelProperty(Object element, String property)
              {
                return undecoratingLabelProvider.isLabelProperty(element, property);
              }

              @Override
              public void dispose()
              {
                undecoratingLabelProvider.dispose();
              }

              @Override
              public void addListener(ILabelProviderListener listener)
              {
                undecoratingLabelProvider.addListener(listener);
              }

              @Override
              public String getText(Object element)
              {
                return undecoratingLabelProvider.getUndecoratedText(element);
              }

              @Override
              public Image getImage(Object element)
              {
                return undecoratingLabelProvider.getUndecoratedImage(element);
              }
            };
        }

        buildToolTipMessage(builder, labelProvider, element, d);
        return builder.toString();
      }
      return null;
    }

    @Override
    public void update(ViewerCell cell) {
      // TODO Auto-generated method stub

    }

  }

  public void reset() {
    stageViewer.setInput(null);
    destinationViewer.setInput(null);
    if (stage != null) {
      if (listener != null) {
        stage.removeStageListener(listener);
      }
      stage.dispose();
      stage = null;
    }
  }

  class LabelProvider extends AdapterFactoryLabelProvider.FontAndColorProvider {

  public LabelProvider(AdapterFactory adapterFactory, Viewer viewer) {
      super(adapterFactory, viewer);
  }

    Font boldFont = null;
    Font italicFont = null;

    @Override
    public void dispose() {
      if (boldFont != null) {
        boldFont.dispose();
      }
    }

    @Override
    public Font getFont(Object object) {
      if (stage.getElements().contains(object)) {
        if (boldFont == null) {
          FontDescriptor descriptor = FontDescriptor.createFrom(getDefaultFont()).setStyle(SWT.BOLD);
          boldFont = descriptor.createFont(getDefaultFont().getDevice());
        }
        return boldFont;
      } else if (object instanceof EObject && EcoreUtil.isAncestor(stage.getElements(), (EObject) object)) {
        return getDefaultFont();
      } else {
        if (italicFont == null) {
          FontDescriptor descriptor = FontDescriptor.createFrom(getDefaultFont()).setStyle(SWT.ITALIC);
          italicFont = descriptor.createFont(getDefaultFont().getDevice());
        }
        return italicFont;
      }

    }

    @Override
    public Color getForeground(Object object) {
      if (stage.hasBackreferences((EObject) object)) {
        return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
      } else if (stage.getNewParent((EObject) object) != null) {
        return FlexibilityColors.getColorRegistry().get(FlexibilityColors.FG_GREEN);
      }
      if (!EcoreUtil.isAncestor(stage.getElements(), (EObject) object)) {
        return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
      }
      return super.getForeground(object);
    }
  }

  @Override
  public void addSelectionChangedListener(ISelectionChangedListener listener) {
     stageViewer.addSelectionChangedListener(listener);
    destinationViewer.addSelectionChangedListener(listener);
  }

  @Override
  public ISelection getSelection() {
    return stageViewer.getControl().isFocusControl() ? stageViewer.getSelection() : destinationViewer.getSelection();
  }

  @Override
  public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    stageViewer.removeSelectionChangedListener(listener);
    destinationViewer.removeSelectionChangedListener(listener);
  }

  @Override
  public void setSelection(ISelection selection) {
    stageViewer.setSelection(selection);
  }

  @Override
  public String getContributorId() {
    return "org.polarsys.capella.core.data.capellamodeller.properties"; //$NON-NLS-1$
  }

  @Override
  public Object getAdapter(Class adapter) {
    if (adapter == IPropertySheetPage.class) {
      return new TabbedPropertySheetPage(this);
    }
    return super.getAdapter(adapter);
  }

  @Override
  public void dispose() {
    cfac.dispose();
    if (toolkit != null) {
      toolkit.dispose();
    }
    if (stage != null) {
      stage.dispose();
    }
    if (tooltipSupport != null) {
      tooltipSupport.dispose();
    }
  }

  private static class MyLocateInCapellaExplorerAction extends LocateInCapellaExplorerAction {
    @Override
    public void selectElementInCapellaExplorer(ISelection selection) {
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      try {
        window.getActivePage().showView(CapellaCommonNavigator.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
      } catch (PartInitException e) {
        Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
      }
      super.selectElementInCapellaExplorer(selection);
    }
  }
  
  private static class MyDiagnosticDialog extends DiagnosticDialog {

    public MyDiagnosticDialog(Shell parent, Diagnostic diagnostic) {
      super(parent, Messages.ValidateExecuteListener_dialogTitle, Messages.ValidateExecuteListener_dialogMessage, diagnostic, Diagnostic.ERROR | Diagnostic.WARNING); 
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
      createDetailsButton(parent);
      createButton(parent, IDialogConstants.OK_ID, Messages.ValidateExecuteListener_dialogProceedButton, false);
      createButton(parent, IDialogConstants.CANCEL_ID, Messages.ValidateExecuteListener_dialogCancelButton, true);
    }

  }


}
