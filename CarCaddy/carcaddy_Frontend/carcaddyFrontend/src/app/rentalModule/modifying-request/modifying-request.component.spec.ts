import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModifyingRequestComponent } from './modifying-request.component';

describe('ModifyingRequestComponent', () => {
  let component: ModifyingRequestComponent;
  let fixture: ComponentFixture<ModifyingRequestComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ModifyingRequestComponent]
    });
    fixture = TestBed.createComponent(ModifyingRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
