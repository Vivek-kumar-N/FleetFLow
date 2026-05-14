import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InterceptorsComponent } from './interceptors.component';

describe('InterceptorsComponent', () => {
  let component: InterceptorsComponent;
  let fixture: ComponentFixture<InterceptorsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [InterceptorsComponent]
    });
    fixture = TestBed.createComponent(InterceptorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
